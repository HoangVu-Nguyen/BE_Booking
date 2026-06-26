package clyvasync.Clyvasync.repository.wallet;

import clyvasync.Clyvasync.dto.projection.LedgerTransactionProjection;
import clyvasync.Clyvasync.enums.wallet.TransactionStatus;
import clyvasync.Clyvasync.enums.wallet.TransactionType;
import clyvasync.Clyvasync.modules.wallet.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction,Long> {
    // Lấy toàn bộ giao dịch của một ví, sắp xếp mới nhất lên đầu
    Page<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);

    // Lọc giao dịch theo ví VÀ loại giao dịch, sắp xếp mới nhất lên đầu
    Page<WalletTransaction> findByWalletIdAndTransactionTypeOrderByCreatedAtDesc(Long walletId, TransactionType transactionType, Pageable pageable);
    Page<WalletTransaction> findByTransactionTypeAndStatus(TransactionType type, TransactionStatus status,Pageable pageable);
    @Query("SELECT SUM(w.pendingBalance + w.availableBalance) FROM HostWallet w")
    BigDecimal sumTotalHostDebt();

    @Query("SELECT SUM(wt.amount) FROM WalletTransaction wt WHERE wt.transactionType = 'REFUND_DEDUCTION'")
    BigDecimal sumTotalRefunds();
    @Query(value = """
        SELECT wt.id as txnId, 
               wt.created_at as txnDate, 
               wt.transaction_type as type, 
               wt.status as status, 
               wt.amount as txnAmount,
               b.booking_code as bookingCode, 
               b.total_price as gross, 
               b.platform_fee_amount as fee, 
               b.host_payout_amount as net,
               u_guest.full_name as guestName, 
               u_guest.avatar_url as guestAvatar,
               u_host.full_name as hostName, 
               wt.bank_account_info as bankInfo
        FROM wallet_transactions wt
        LEFT JOIN bookings b ON wt.booking_id = b.id
        LEFT JOIN users u_guest ON b.user_id = u_guest.id
        JOIN host_wallets hw ON wt.wallet_id = hw.id
        JOIN users u_host ON hw.owner_id = u_host.id
        WHERE (:search IS NULL OR b.booking_code ILIKE '%' || :search || '%' OR u_guest.full_name ILIKE '%' || :search || '%')
          AND (:type IS NULL OR wt.transaction_type = :type)
        ORDER BY wt.created_at DESC
    """, nativeQuery = true)
    Page<LedgerTransactionProjection> findLedgerTransactions(@Param("search") String search, @Param("type") String type, Pageable pageable);
}
