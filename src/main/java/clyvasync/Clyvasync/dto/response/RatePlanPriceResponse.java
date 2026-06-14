package clyvasync.Clyvasync.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class RatePlanPriceResponse {

    private Long ratePlanId;

    private String name;

    // Giá thực tế của ngày đó: nếu có override thì lấy override, không thì lấy basePrice
    private BigDecimal price;

    // Giá gốc trong room_rate_plans
    private BigDecimal basePrice;

    // Có override riêng cho ngày này không
    private Boolean hasOverride;
}