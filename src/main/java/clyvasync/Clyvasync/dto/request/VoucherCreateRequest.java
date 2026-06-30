package clyvasync.Clyvasync.dto.request;

import clyvasync.Clyvasync.enums.offer.DiscountType;
import clyvasync.Clyvasync.enums.offer.SponsorType;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class VoucherCreateRequest {
    private String code;

    @NotBlank(message = "Name is required")
    private String name;

    private String description;

    @NotNull(message = "Discount type is required")
    private DiscountType discountType;

    @NotNull(message = "Discount value is required")
    private BigDecimal discountValue;

    private BigDecimal maxDiscount;
    private BigDecimal minOrderValue;
    private Integer pointsRequired;
    private SponsorType sponsorType;
    private OffsetDateTime validFrom;
    private OffsetDateTime validUntil;
    private Integer totalIssueLimit;
    private Integer totalUsageLimit;
}
