package clyvasync.Clyvasync.service.wallet.impl;

import clyvasync.Clyvasync.dto.detail.WalletNotificationPayload;
import clyvasync.Clyvasync.dto.event.WalletEvent;
import clyvasync.Clyvasync.dto.request.WithdrawApprovalRequest;
import clyvasync.Clyvasync.enums.type.PayoutStatus;
import clyvasync.Clyvasync.enums.wallet.TransactionStatus;
import clyvasync.Clyvasync.enums.wallet.TransactionType;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.booking.entity.Booking;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import clyvasync.Clyvasync.modules.wallet.entity.HostWallet;
import clyvasync.Clyvasync.modules.wallet.entity.WalletTransaction;
import clyvasync.Clyvasync.repository.booking.BookingRepository;
import clyvasync.Clyvasync.repository.homestay.HomestayRepository;
import clyvasync.Clyvasync.repository.wallet.HostWalletRepository;
import clyvasync.Clyvasync.service.realtime.SocketEmitterService;
import clyvasync.Clyvasync.service.wallet.HostWalletService;
import clyvasync.Clyvasync.service.wallet.WalletTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
public class HostWalletServiceImpl implements HostWalletService {

    private final HostWalletRepository hostWalletRepository;
    private final WalletTransactionService walletTransactionService;
    private final BookingRepository bookingRepository;
    private final HomestayRepository homestayRepository;
    private final SocketEmitterService socketEmitterService;
    private final ApplicationEventPublisher eventPublisher;

    public HostWalletServiceImpl(HostWalletRepository hostWalletRepository, @Lazy WalletTransactionService walletTransactionService, BookingRepository bookingRepository, HomestayRepository homestayRepository, SocketEmitterService socketEmitterService, ApplicationEventPublisher eventPublisher) {
        this.hostWalletRepository = hostWalletRepository;
        this.walletTransactionService = walletTransactionService;
        this.bookingRepository = bookingRepository;
        this.homestayRepository = homestayRepository;
        this.socketEmitterService = socketEmitterService;
        this.eventPublisher = eventPublisher;
    }

    @Override
    @Transactional
    public HostWallet getOrCreateWalletByOwnerId(Long ownerId) {
        return hostWalletRepository.findByOwnerIdForUpdate(ownerId)
                .orElseGet(() -> {
                    HostWallet newWallet = HostWallet.builder()
                            .ownerId(ownerId)
                            .pendingBalance(BigDecimal.ZERO)
                            .availableBalance(BigDecimal.ZERO)
                            .totalWithdrawn(BigDecimal.ZERO)
                            .build();
                    return hostWalletRepository.save(newWallet);
                });
    }

    @Override
    public HostWallet getWalletInfo(Long ownerId) {
        return hostWalletRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new AppException(ResultCode.WALLET_NOT_FOUND));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void lockFundsForBooking(Long bookingId, Long ownerId, BigDecimal amount) {
        // 1. Lấy hoặc tạo ví
        HostWallet wallet = getOrCreateWalletByOwnerId(ownerId);

        // 2. Cộng tiền vào Pending Balance (Ký quỹ)
        wallet.setPendingBalance(wallet.getPendingBalance().add(amount));
        hostWalletRepository.save(wallet);

        // 3. Ghi log giao dịch
        walletTransactionService.logTransaction(
                wallet.getId(),
                bookingId,
                amount,
                TransactionType.BOOKING_REVENUE,
                TransactionStatus.PENDING,
                "Ký quỹ doanh thu từ Booking #" + bookingId,
                null
        );
    }



    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deductFundsForRefund(Long bookingId, BigDecimal refundAmount) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ResultCode.BOOKING_NOT_FOUND));

        Homestay homestay = homestayRepository.findById(booking.getHomestayId())
                .orElseThrow(() -> new  AppException(ResultCode.HOMESTAY_NOT_FOUND));

        HostWallet wallet = getWalletInfo(homestay.getOwnerId());

        // Nếu tiền đang bị giam -> trừ Pending. Nếu đã giải ngân -> trừ Available.
        if (booking.getPayoutStatus() == PayoutStatus.ON_HOLD) {
            wallet.setPendingBalance(wallet.getPendingBalance().subtract(refundAmount));
        } else {
            // Chú ý: Trừ Available có thể làm âm ví nếu Host rút hết tiền rồi.
            // Ở hệ thống lớn, họ cho phép âm ví để cấn trừ vào booking sau.
            wallet.setAvailableBalance(wallet.getAvailableBalance().subtract(refundAmount));
        }

        hostWalletRepository.save(wallet);

        walletTransactionService.logTransaction(
                wallet.getId(),
                bookingId,
                refundAmount,
                TransactionType.REFUND_DEDUCTION,
                TransactionStatus.COMPLETED,
                "Khấu trừ hoàn tiền khách hủy từ Booking #" + booking.getBookingCode(),
                null
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void requestWithdrawal(Long ownerId, BigDecimal amount, String bankAccountInfo) {
        // 1. Kiểm tra ví có tồn tại không trước
        HostWallet wallet = hostWalletRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> new AppException(ResultCode.WALLET_NOT_FOUND));

        // 2. Thực hiện trừ tiền trực tiếp bằng câu lệnh SQL nguyên tử
        BigDecimal standardizedAmount = amount.setScale(2, java.math.RoundingMode.HALF_UP);

        // Log ra để bác kiểm tra xem số đã có đuôi .00 chưa
        log.info("[WITHDRAW] Số tiền gốc: {} | Số tiền sau chuẩn hóa: {}", amount, standardizedAmount);

        // 3. Truyền số đã chuẩn hóa vào câu lệnh nguyên tử
        int rowsUpdated = hostWalletRepository.deductAvailableBalance(ownerId, standardizedAmount);

        // Nếu số dòng update = 0, nghĩa là số dư khả dụng không đủ
        if (rowsUpdated == 0) {
            // Log thêm thông tin số dư hiện tại trong thực tế để đối soát
            log.warn("[WITHDRAW THẤT BẠI] HostId: {} | Khả dụng trong DB Entity: {}", ownerId, wallet.getAvailableBalance());
            throw new AppException(ResultCode.INSUFFICIENT_AVAILABLE_BALANCE);
        }

        // 3. Ghi log lệnh rút (Mọi thứ an toàn 100%)
        walletTransactionService.logTransaction(
                wallet.getId(),
                null,
                amount,
                TransactionType.WITHDRAWAL,
                TransactionStatus.PENDING,
                "Yêu cầu rút tiền về ngân hàng.",
                bankAccountInfo
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void releaseEscrowFunds(Long bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new AppException(ResultCode.BOOKING_NOT_FOUND));

        Homestay homestay = homestayRepository.findById(booking.getHomestayId())
                .orElseThrow(() -> new AppException(ResultCode.HOMESTAY_NOT_FOUND));

        HostWallet wallet = hostWalletRepository.findByOwnerId(homestay.getOwnerId())
                .orElseThrow(() -> new AppException(ResultCode.WALLET_NOT_FOUND));

        BigDecimal amount = booking.getHostPayoutAmount();

        // Thực hiện dịch chuyển dòng tiền nguyên tử dưới DB
        int rowsUpdated = hostWalletRepository.releaseEscrowFunds(homestay.getOwnerId(), amount);
        if (rowsUpdated == 0) {
            throw new RuntimeException("Giải ngân thất bại, số dư ký quỹ không đủ cấn trừ!");
        }

        // Cập nhật trạng thái giải ngân của Booking
        booking.setPayoutStatus(PayoutStatus.ELIGIBLE);
        bookingRepository.save(booking);

        // Ghi log giao dịch thành công
        walletTransactionService.logTransaction(
                wallet.getId(),
                bookingId,
                amount,
                TransactionType.ESCROW_RELEASE,
                TransactionStatus.COMPLETED,
                "Giải ngân doanh thu từ Booking #" + booking.getBookingCode(),
                null
        );
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void approveWithdrawal(Long transactionId) {
        // 1. Tìm giao dịch rút tiền
        WalletTransaction transaction = walletTransactionService.getById(transactionId);


        if (!TransactionStatus.PENDING.equals(transaction.getStatus()) || !TransactionType.WITHDRAWAL.equals(transaction.getTransactionType())) {
            throw new AppException(ResultCode.INVALID_TRANSACTION_STATUS);
        }

        // 2. Cập nhật trạng thái giao dịch thành thành công
        transaction.setStatus(TransactionStatus.COMPLETED);
        transaction.setDescription(transaction.getDescription() + " - Đã duyệt bởi Admin.");
        walletTransactionService.save(transaction);

        // 3. Cộng dồn vào tổng số tiền đã rút của ví để làm báo cáo tài chính
        HostWallet wallet = hostWalletRepository.findById(transaction.getWalletId())
                .orElseThrow(() -> new AppException(ResultCode.WALLET_NOT_FOUND));
        wallet.setTotalWithdrawn(wallet.getTotalWithdrawn().add(transaction.getAmount()));
        hostWalletRepository.save(wallet);
        WalletNotificationPayload payload = WalletNotificationPayload.builder()
                .type(TransactionType.WITHDRAW_APPROVED)
                .transactionId(transactionId)
                .amount(transaction.getAmount())
                .status(TransactionStatus.COMPLETED)
                .message(String.format("Yêu cầu rút %,.0f ₫ của bạn đã được phê duyệt thành công!", transaction.getAmount()))
                .build();

        log.info("[SERVICE] Phát sự kiện duyệt tiền cho HostId: {}", wallet.getOwnerId());
        eventPublisher.publishEvent(new WalletEvent(this, wallet.getOwnerId(), payload));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void rejectWithdrawal(Long transactionId, String reason) {
        // 1. Tìm giao dịch
        WalletTransaction transaction = walletTransactionService.getById(transactionId);

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            throw new AppException(ResultCode.INVALID_TRANSACTION_STATUS);
        }

        // 2. Trả lại tiền vào tài khoản khả dụng cho Host
        HostWallet wallet = hostWalletRepository.findById(transaction.getWalletId())
                .orElseThrow(() ->  new AppException(ResultCode.WALLET_NOT_FOUND));

        wallet.setAvailableBalance(wallet.getAvailableBalance().add(transaction.getAmount()));
        hostWalletRepository.save(wallet);

        // 3. Cập nhật trạng thái lệnh thành FAILED
        transaction.setStatus(TransactionStatus.FAILED);
        transaction.setDescription("Bị từ chối: " + reason);
        walletTransactionService.save(transaction);
        WalletNotificationPayload payload = WalletNotificationPayload.builder()
                .type(TransactionType.WITHDRAW_REJECTED)
                .transactionId(transactionId)
                .amount(transaction.getAmount())
                .status(TransactionStatus.FAILED)
                .message(String.format("Yêu cầu rút %,.0f ₫ bị từ chối. Lý do: %s", transaction.getAmount(), reason))
                .build();

        log.info("[SERVICE] Phát sự kiện từ chối rút tiền cho HostId: {}", wallet.getOwnerId());
        eventPublisher.publishEvent(new WalletEvent(this, wallet.getOwnerId(), payload));
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void resolveWithdrawal(WithdrawApprovalRequest request) {
        TransactionStatus action;
        try {
            action = TransactionStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            // Ném lỗi nếu Admin truyền linh tinh (không phải COMPLETED/FAILED)
            throw new AppException(ResultCode.INVALID_TRANSACTION_STATUS);
        }

        if (action == TransactionStatus.COMPLETED) {
            // Gọi hàm duyệt nội bộ
            this.approveWithdrawal(request.getTransactionId());
        } else if (action == TransactionStatus.FAILED) {
            // Gọi hàm từ chối nội bộ
            this.rejectWithdrawal(request.getTransactionId(), request.getAdminComment());
        } else {
            throw new AppException(ResultCode.INVALID_TRANSACTION_STATUS);
        }
    }

    @Override
    public Optional<HostWallet> findByOwnerIdForUpdate(Long ownerId) {
        return hostWalletRepository.findByOwnerIdForUpdate(ownerId);
    }

    @Override
    public HostWallet findAndLockByOwnerId(Long hostId) {
        return hostWalletRepository.findAndLockByOwnerId(hostId).orElseThrow(() -> new AppException(ResultCode.WALLET_NOT_FOUND));
    }

    @Override
    public HostWallet save(HostWallet hostWallet) {
        return hostWalletRepository.save(hostWallet);
    }
}