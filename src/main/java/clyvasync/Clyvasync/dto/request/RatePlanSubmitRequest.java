package clyvasync.Clyvasync.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
@Data
public class RatePlanSubmitRequest {
    private Long id; // null = tạo mới

    private String name; // Standard, Luxury...
    private BigDecimal price;

    // true = không hoàn hủy, false = có thể hoàn hủy
    private Boolean isNonRefundable;

    // Các dòng tích xanh hiển thị cho khách
    private List<String> benefits;
}
