package clyvasync.Clyvasync.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public  class RatePlanResponse {
    private Long id;
    private String name;        // 'Standard', 'Luxury'
    private BigDecimal price;
    private Boolean isNonRefundable;
    private List<String> benefits; // Danh sách các text tích xanh
}