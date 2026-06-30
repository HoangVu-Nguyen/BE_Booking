package clyvasync.Clyvasync.modules.voucher.entity;

import clyvasync.Clyvasync.enums.offer.PointTransactionType;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_point_history")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserPointHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 20)
    private PointTransactionType transactionType;

    @Column(nullable = false)
    private Integer points;

    @Column(name = "reference_id")
    private Long referenceId;

    private String description;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}