package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.enums.offer.DiscountType;
import clyvasync.Clyvasync.enums.offer.SponsorType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class VoucherResponse {
    private Long id;
    private String code;
    private String name;
    private String description;
    private DiscountType discountType; // "PERCENTAGE" | "FIXED_AMOUNT"
    private BigDecimal discountValue;
    private BigDecimal maxDiscount;
    private BigDecimal minOrderValue;
    private Integer pointsRequired;
    private SponsorType sponsorType; // "MEMBER_REWARD" | "REFERRAL_SPONSOR" | "AGENT_SPONSOR"
    private OffsetDateTime validFrom;
    private OffsetDateTime validUntil;
    private Integer totalIssueLimit;
    private Integer currentIssueCount;
    private Integer totalUsageLimit;
    private Integer currentUsageCount;
    private Boolean isActive;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
    }