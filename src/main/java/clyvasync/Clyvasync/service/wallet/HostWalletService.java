package clyvasync.Clyvasync.service.wallet;

import clyvasync.Clyvasync.dto.request.WithdrawApprovalRequest;
import clyvasync.Clyvasync.modules.wallet.entity.HostWallet;

import java.math.BigDecimal;
import java.util.Optional;

public interface HostWalletService {

    // 1. Khởi tạo hoặc lấy ví hiện tại của Host
    HostWallet getOrCreateWalletByOwnerId(Long ownerId);

    // 2. Lấy thông tin ví (Dùng để hiển thị lên Dashboard của Host)
    HostWallet getWalletInfo(Long ownerId);

    // ==========================================
    // ESCROW FLOW (LUỒNG KÝ QUỸ TỰ ĐỘNG)
    // ==========================================

    /**
     * BƯỚC 1: Khách thanh toán thành công.
     * Cộng tiền vào PENDING_BALANCE (tiền giam).
     */
    void lockFundsForBooking(Long bookingId, Long ownerId, BigDecimal amount);

    /**
     * BƯỚC 2: Cronjob gọi hàm này khi Khách ở qua 24h mà không khiếu nại.
     * Trừ ở PENDING_BALANCE, cộng sang AVAILABLE_BALANCE.
     */
    void releaseEscrowFunds(Long bookingId);

    /**
     * BƯỚC TÙY CHỌN: Khách khiếu nại thành công hoặc hủy phòng hợp lệ.
     * Trừ tiền từ ví của Host (nếu đã khóa thì trừ pending, nếu lỡ giải ngân thì trừ available).
     */
    void deductFundsForRefund(Long bookingId, BigDecimal refundAmount);

    // ==========================================
    // WITHDRAWAL FLOW (LUỒNG RÚT TIỀN)
    // ==========================================

    /**
     * Host đặt lệnh rút tiền về ngân hàng.
     * Kiểm tra Available Balance có đủ không, trừ tiền và ghi log.
     */
    void requestWithdrawal(Long ownerId, BigDecimal amount, String bankAccountInfo);
    void approveWithdrawal(Long transactionId);
    void rejectWithdrawal(Long transactionId, String reason);
    void resolveWithdrawal(WithdrawApprovalRequest request);

    Optional<HostWallet> findByOwnerIdForUpdate(Long ownerId);
}