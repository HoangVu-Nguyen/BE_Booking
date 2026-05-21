package clyvasync.Clyvasync.repository.wallet;

import clyvasync.Clyvasync.enums.wallet.TransactionStatus;
import clyvasync.Clyvasync.enums.wallet.TransactionType;
import clyvasync.Clyvasync.modules.wallet.entity.WalletTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletTransactionRepository extends JpaRepository<WalletTransaction,Long> {
    // Lấy toàn bộ giao dịch của một ví, sắp xếp mới nhất lên đầu
    Page<WalletTransaction> findByWalletIdOrderByCreatedAtDesc(Long walletId, Pageable pageable);

    // Lọc giao dịch theo ví VÀ loại giao dịch, sắp xếp mới nhất lên đầu
    Page<WalletTransaction> findByWalletIdAndTransactionTypeOrderByCreatedAtDesc(Long walletId, TransactionType transactionType, Pageable pageable);
    Page<WalletTransaction> findByTransactionTypeAndStatus(TransactionType type, TransactionStatus status,Pageable pageable);
}
