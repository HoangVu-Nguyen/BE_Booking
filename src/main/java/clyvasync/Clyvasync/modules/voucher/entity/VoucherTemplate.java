package clyvasync.Clyvasync.modules.voucher.entity;

import clyvasync.Clyvasync.enums.offer.DiscountType;
import clyvasync.Clyvasync.enums.offer.SponsorType;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Table(name = "voucher_templates")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VoucherTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, length = 50)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", nullable = false, length = 20)
    private DiscountType discountType;

    @Column(name = "discount_value", nullable = false, precision = 19, scale = 2)
    private BigDecimal discountValue;

    @Column(name = "max_discount", precision = 19, scale = 2)
    private BigDecimal maxDiscount;

    @Column(name = "min_order_value", precision = 19, scale = 2)
    private BigDecimal minOrderValue;

    @Column(name = "points_required", nullable = false)
    private Integer pointsRequired;

    @Enumerated(EnumType.STRING)
    @Column(name = "sponsor_type", length = 20)
    private SponsorType sponsorType;

    @Column(name = "valid_from")
    private OffsetDateTime validFrom;

    @Column(name = "valid_until")
    private OffsetDateTime validUntil;

    @Column(name = "is_active")
    private Boolean isActive;
    @Column(name = "total_issue_limit")
    private Integer totalIssueLimit;

    @Column(name = "current_issue_count")
    private Integer currentIssueCount;

    @Column(name = "total_usage_limit")
    private Integer totalUsageLimit;

    @Column(name = "current_usage_count")
    private Integer currentUsageCount;

    @Version
    @Column(nullable = false)
    private Integer version;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
        this.updatedAt = OffsetDateTime.now();
        if (this.isActive == null) this.isActive = true;
        if (this.minOrderValue == null) this.minOrderValue = BigDecimal.ZERO;
        if (this.pointsRequired == null) this.pointsRequired = 0;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }
}