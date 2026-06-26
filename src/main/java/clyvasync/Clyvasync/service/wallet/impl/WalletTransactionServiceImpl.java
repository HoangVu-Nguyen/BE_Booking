package clyvasync.Clyvasync.service.wallet.impl;

import clyvasync.Clyvasync.dto.projection.LedgerTransactionProjection;
import clyvasync.Clyvasync.dto.response.TransactionResponse;
import clyvasync.Clyvasync.enums.type.PaymentStatus;
import clyvasync.Clyvasync.enums.wallet.TransactionStatus;
import clyvasync.Clyvasync.enums.wallet.TransactionType;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.booking.entity.BookingDetail;
import clyvasync.Clyvasync.modules.room.RoomRatePlan;
import clyvasync.Clyvasync.modules.wallet.entity.HostWallet;
import clyvasync.Clyvasync.modules.wallet.entity.WalletTransaction;
import clyvasync.Clyvasync.repository.wallet.WalletTransactionRepository;
import clyvasync.Clyvasync.service.homestay.HomestayService;
import clyvasync.Clyvasync.service.room.RoomRatePlanService;
import clyvasync.Clyvasync.service.wallet.HostWalletService;
import clyvasync.Clyvasync.service.wallet.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class WalletTransactionServiceImpl implements WalletTransactionService {

    private final WalletTransactionRepository walletTransactionRepository;
    private final HomestayService homestayService;
    private final RoomRatePlanService roomRatePlanService;
    private final HostWalletService hostWalletService;

    @Override
    @Transactional
    public WalletTransaction logTransaction(Long walletId, Long bookingId, BigDecimal amount,
                                            TransactionType type, TransactionStatus status, String description,
                                            String bankAccountInfo) {
        // Tạo một bản ghi giao dịch mới sử dụng Builder pattern
        WalletTransaction transaction = WalletTransaction.builder()
                .walletId(walletId)
                .bookingId(bookingId)
                .amount(amount)
                .transactionType(type)
                .status(status)
                .description(description)
                .bankAccountInfo(bankAccountInfo)
                .build();

        // Lưu vào Database và trả về Entity đã lưu
        return walletTransactionRepository.save(transaction);
    }

    @Override
    public Page<WalletTransaction> getTransactionHistory(Long walletId, Pageable pageable) {
        // Trả về lịch sử giao dịch (thường sẽ được sort giảm dần theo createdAt từ Controller)
        return walletTransactionRepository.findByWalletIdOrderByCreatedAtDesc(walletId, pageable);
    }

    @Override
    public Page<WalletTransaction> getTransactionHistoryByType(Long walletId, TransactionType type, Pageable pageable) {
        // Lọc giao dịch theo loại (VD: Chỉ lấy các lệnh rút tiền 'WITHDRAWAL')
        return walletTransactionRepository.findByWalletIdAndTransactionTypeOrderByCreatedAtDesc(walletId, type, pageable);
    }

    @Override
    @Transactional
    public void updateTransactionStatus(Long transactionId, TransactionStatus status) {
        // Tìm giao dịch, nếu không có thì ném lỗi
        WalletTransaction transaction = walletTransactionRepository.findById(transactionId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy giao dịch mã: " + transactionId));

        // Cập nhật trạng thái mới (VD: PENDING -> COMPLETED)
        transaction.setStatus(status);
        walletTransactionRepository.save(transaction);
    }

    @Override
    public WalletTransaction getById(Long id) {
        return walletTransactionRepository.findById(id).orElseThrow(() -> new AppException(ResultCode.TRANSACTION_NOT_FOUND));
    }

    @Override
    public void save(WalletTransaction walletTransaction) {
        walletTransactionRepository.save(walletTransaction);
    }

    @Override
    public Page<WalletTransaction> findByTransactionTypeAndStatus(TransactionType type, TransactionStatus status, Pageable pageable) {
        return walletTransactionRepository.findByTransactionTypeAndStatus(type,status,pageable);
    }
    @Transactional(propagation = Propagation.REQUIRED)
    public PaymentStatus processCancellationRefund(Long homestayId, Long bookingId, String bookingCode, BigDecimal totalPaid, BookingDetail detail) {

        // 1. Lấy chính sách gói giá
        RoomRatePlan ratePlan = roomRatePlanService.getById(detail.getRatePlanId());

        // 2. Tính toán tiền hoàn dựa trên thời gian thực tế
        BigDecimal refundAmount = BigDecimal.ZERO;

        if (Boolean.FALSE.equals(ratePlan.getIsNonRefundable())) {
            long daysUntilCheckIn = ChronoUnit.DAYS.between(LocalDate.now(), detail.getCheckInDate());

            if (daysUntilCheckIn >= 7) {
                refundAmount = totalPaid; // Hủy trước 7 ngày: Trả 100%
            } else if (daysUntilCheckIn >= 3) {
                refundAmount = totalPaid.multiply(new BigDecimal("0.50")); // Hủy từ 3-6 ngày: Trả 50%
            }
        }

        BigDecimal hostPenaltyRevenue = totalPaid.subtract(refundAmount);

        // 3. Xử lý cập nhật Ví Chủ Nhà (Pessimistic Lock trên Ví để tránh lỗi race condition khi cộng/trừ tiền)
        Long hostId = homestayService.getOwnerIdByHomestayId(homestayId);
        HostWallet wallet = hostWalletService.findAndLockByOwnerId(hostId);

        // Trừ toàn bộ tiền của booking ra khỏi pending_balance (vì đơn đã hủy, không giữ nữa)
        wallet.setPendingBalance(wallet.getPendingBalance().subtract(totalPaid));

        // Nếu chủ nhà được hưởng tiền phạt, cộng ngay vào available_balance
        if (hostPenaltyRevenue.compareTo(BigDecimal.ZERO) > 0) {
            wallet.setAvailableBalance(wallet.getAvailableBalance().add(hostPenaltyRevenue));
        }
        hostWalletService.save(wallet);

        // 4. Ghi log lịch sử giao dịch (Audit Trail)
        // Record 1: Rút tiền khỏi Pending
        createTransactionRecord(wallet.getId(), bookingId, totalPaid.negate(), TransactionType.REFUND_DEDUCTION,
                "Khách hủy phòng - Rút khỏi Pending. Mã: " + bookingCode);

        // Record 2: Cộng doanh thu tiền phạt (Nếu có)
        if (hostPenaltyRevenue.compareTo(BigDecimal.ZERO) > 0) {
            createTransactionRecord(wallet.getId(), bookingId, hostPenaltyRevenue, TransactionType.CANCELLATION_FEE_REVENUE,
                    "Thu nhập từ phí phạt khách hủy phòng. Mã: " + bookingCode);
        }

        // 5. Trả về trạng thái thanh toán mới cho Booking
        if (refundAmount.compareTo(BigDecimal.ZERO) == 0) {
            return PaymentStatus.NON_REFUNDABLE;
        } else if (refundAmount.compareTo(totalPaid) == 0) {
            return PaymentStatus.REFUND_PENDING; // Chờ hệ thống thực hiện lệnh hoàn tiền (qua VNPay/Momo)
        } else {
            return PaymentStatus.PARTIALLY_REFUNDED;
        }
    }

    @Override
    public Page<TransactionResponse> getTransactions(String search, String type, int page, int size) {
        Page<LedgerTransactionProjection> rawData = walletTransactionRepository.findLedgerTransactions(
                search, type, PageRequest.of(page, size)
        );

        return rawData.map(row -> {
            String uiType = switch (row.type()) {
                case "BOOKING_REVENUE" -> "PAYMENT_IN";
                case "WITHDRAWAL" -> "PAYOUT_OUT";
                default -> "REFUND";
            };

            BigDecimal gross = row.gross() != null ? row.gross() : row.txnAmount();
            BigDecimal platformFee = row.fee() != null ? row.fee() : BigDecimal.ZERO;
            BigDecimal netToHost = row.net() != null ? row.net() : row.txnAmount();

            return TransactionResponse.builder()
                    .id("TXN-" + String.format("%06d", row.txnId()))
                    .date(row.txnDate())
                    .type(uiType)
                    .status(row.status())

                    .guest(row.guestName() != null ? TransactionResponse.GuestDto.builder()
                            .name(row.guestName())
                            .avatar(row.guestAvatar())
                            .build() : null)

                    .host(TransactionResponse.HostDto.builder()
                            .name(row.hostName())
                            .build())

                    .paymentDetails(parsePaymentInfo(row.type(), row.bankInfo()))
                    .amounts(TransactionResponse.AmountsDto.builder()
                            .gross(gross)
                            .platformFee(platformFee)
                            .netToHost(netToHost)
                            .build())
                    .build();
        });
    }


    private TransactionResponse.PaymentDetailsDto parsePaymentInfo(String dbType, String bankInfo) {
        if ("BOOKING_REVENUE".equals(dbType)) {
            return TransactionResponse.PaymentDetailsDto.builder()
                    .method("VNPay")
                    .build();
        }

        if (bankInfo != null && bankInfo.contains("-")) {
            String[] parts = bankInfo.split("-");
            String bankName = parts[0].trim();

            String last4 = "";
            if (parts.length > 1) {
                String accountNum = parts[1].trim();
                last4 = accountNum.length() > 4 ? accountNum.substring(accountNum.length() - 4) : accountNum;
            }

            return TransactionResponse.PaymentDetailsDto.builder()
                    .method("BANK")
                    .bank(bankName)
                    .last4(last4)
                    .build();
        }

        return TransactionResponse.PaymentDetailsDto.builder()
                .method("SYSTEM")
                .build();
    }

    private void createTransactionRecord(Long walletId, Long bookingId, BigDecimal amount, TransactionType type, String desc) {
        WalletTransaction tx = new WalletTransaction();
        tx.setWalletId(walletId);
        tx.setBookingId(bookingId);
        tx.setAmount(amount);
        tx.setTransactionType(type);
        tx.setStatus(TransactionStatus.COMPLETED);
        tx.setDescription(desc);
        walletTransactionRepository.save(tx);
    }
}