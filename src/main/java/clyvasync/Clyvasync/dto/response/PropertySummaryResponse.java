package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.dto.detail.PropertyStats;
import clyvasync.Clyvasync.enums.homestay.HomestayStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PropertySummaryResponse {
    private Long id;
    private String name;
    private String type;       // VD: "Căn hộ", "Biệt thự"
    private String location;   // VD: "Quận 1, TP. HCM"
    private String image;      // URL ảnh cover
    private BigDecimal price;  // Giá mặc định mỗi đêm
    private HomestayStatus status;     // "ACTIVE", "DRAFT", "CLOSED"

    private PropertyStats stats; // Thống kê đi kèm
}
