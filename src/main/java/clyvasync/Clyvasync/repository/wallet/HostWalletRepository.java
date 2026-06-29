package clyvasync.Clyvasync.repository.wallet;

import clyvasync.Clyvasync.dto.projection.HostWalletProjection;
import clyvasync.Clyvasync.modules.wallet.entity.HostWallet;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface HostWalletRepository extends JpaRepository<HostWallet,Long> {
    Optional<HostWallet> findByOwnerId(Long ownerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT w FROM HostWallet w WHERE w.ownerId = :ownerId")
    Optional<HostWallet> findByOwnerIdForUpdate(Long ownerId);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE HostWallet w SET w.availableBalance = w.availableBalance - :amount WHERE w.ownerId = :ownerId AND w.availableBalance >= :amount")
    int deductAvailableBalance(@Param("ownerId") Long ownerId, @Param("amount") BigDecimal amount);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("UPDATE HostWallet w SET w.pendingBalance = w.pendingBalance - :amount, w.availableBalance = w.availableBalance + :amount WHERE w.ownerId = :ownerId AND w.pendingBalance >= :amount")
    int releaseEscrowFunds(@Param("ownerId") Long ownerId, @Param("amount") BigDecimal amount);
    Optional<HostWallet> findAndLockByOwnerId(Long ownerId);
    @Query("SELECT w.ownerId AS ownerId, w.availableBalance AS balance FROM HostWallet w WHERE w.ownerId IN :ownerIds")
    List<HostWalletProjection> getWalletBalancesByOwners(@Param("ownerIds") List<Long> ownerIds);
}