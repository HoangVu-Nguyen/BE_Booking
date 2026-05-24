package clyvasync.Clyvasync.dto.detail;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public  class PropertyStats {
    private Double rating;      // Điểm đánh giá (VD: 4.8)
    private Integer reviews;    // Số lượt đánh giá (VD: 124)
    private Integer occupancy;  // Tỷ lệ lấp đầy % (VD: 75)
}
