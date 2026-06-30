package clyvasync.Clyvasync.modules.voucher.entity;

import clyvasync.Clyvasync.enums.offer.VoucherStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.OffsetDateTime;

@Entity
@Table(name = "user_vouchers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserVoucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "template_id", nullable = false)
    private Long templateId;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private VoucherStatus status;

    @Column(name = "used_at")
    private OffsetDateTime usedAt;

    @Column(name = "used_on_booking_id")
    private Long usedOnBookingId;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        if (this.status == null) this.status = VoucherStatus.AVAILABLE;
    }
}