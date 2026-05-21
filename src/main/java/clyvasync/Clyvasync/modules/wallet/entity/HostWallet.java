package clyvasync.Clyvasync.modules.wallet.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "host_wallets", indexes = {
        @Index(name = "idx_host_wallets_owner_id", columnList = "owner_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HostWallet {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "owner_id", nullable = false, unique = true)
    private Long ownerId; // ID của chủ nhà

    // Tiền đang bị App giam (Chờ check-in qua 24h)
    @Builder.Default
    @Column(name = "pending_balance", precision = 19, scale = 2, nullable = false)
    private BigDecimal pendingBalance = BigDecimal.ZERO;

    // Tiền Host có thể rút về ngân hàng thực
    @Builder.Default
    @Column(name = "available_balance", precision = 19, scale = 2, nullable = false)
    private BigDecimal availableBalance = BigDecimal.ZERO;

    // Tổng tiền đã rút thành công từ trước đến nay (Dùng để làm báo cáo)
    @Builder.Default
    @Column(name = "total_withdrawn", precision = 19, scale = 2, nullable = false)
    private BigDecimal totalWithdrawn = BigDecimal.ZERO;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 0;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
}