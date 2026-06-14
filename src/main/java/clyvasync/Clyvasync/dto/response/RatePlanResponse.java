package clyvasync.Clyvasync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
public  class RatePlanResponse {
    private Long id;
    private String name;        // 'Standard', 'Luxury'
    private BigDecimal price;
    private Boolean isNonRefundable;
    private List<String> benefits; // Danh sách các text tích xanh

    public RatePlanResponse(Long id, String name, BigDecimal price, Boolean isNonRefundable) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.isNonRefundable = isNonRefundable;
    }
}