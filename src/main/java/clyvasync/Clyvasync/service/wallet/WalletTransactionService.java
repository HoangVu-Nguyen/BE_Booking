package clyvasync.Clyvasync.service.wallet;

import clyvasync.Clyvasync.enums.type.PaymentStatus;
import clyvasync.Clyvasync.enums.wallet.TransactionStatus;
import clyvasync.Clyvasync.enums.wallet.TransactionType;
import clyvasync.Clyvasync.modules.booking.entity.BookingDetail;
import clyvasync.Clyvasync.modules.wallet.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;

public interface WalletTransactionService {

    /**
     * Tạo một giao dịch mới (Ghi nhận cộng/trừ tiền).
     * @param type "BOOKING_REVENUE", "WITHDRAWAL", "REFUND_DEDUCTION"
     * @param status "PENDING", "COMPLETED", "FAILED"
     */
    WalletTransaction logTransaction(Long walletId, Long bookingId, BigDecimal amount,
                                     TransactionType type, TransactionStatus status, String description,
                                     String bankAccountInfo);

    /**
     * Lấy lịch sử giao dịch cho Host (Có phân trang để hiển thị lên App)
     */
    Page<WalletTransaction> getTransactionHistory(Long walletId, Pageable pageable);

    /**
     * Lấy lịch sử giao dịch Lọc theo Loại (VD: Chỉ xem các lệnh rút tiền)
     */
    Page<WalletTransaction> getTransactionHistoryByType(Long walletId, TransactionType type, Pageable pageable);

    /**
     * Cập nhật trạng thái giao dịch (Dùng khi Admin chuyển khoản tay xong,
     * đổi trạng thái lệnh rút tiền từ PENDING sang COMPLETED)
     */
    void updateTransactionStatus(Long transactionId, TransactionStatus status);
    WalletTransaction getById(Long id);
    void save(WalletTransaction walletTransaction);
    Page<WalletTransaction> findByTransactionTypeAndStatus(TransactionType type, TransactionStatus status, Pageable pageable);
    PaymentStatus processCancellationRefund(Long homestayId, Long bookingId, String bookingCode, BigDecimal totalPaid, BookingDetail detail);
}