package clyvasync.Clyvasync.dto.response;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class HostPortfolioSummaryResponse {

    // 1. Giá trị Portfolio / Tổng doanh thu
    private BigDecimal totalPortfolioValue;
    private Double portfolioGrowthRate; // Tỷ lệ tăng trưởng (VD: 12.4)

    // 2. Tỷ lệ lấp đầy
    private Double averageOccupancyRate; // VD: 96.8
    private String occupancyTrend; // VD: "Ổn định", "Tăng nhẹ", "Giảm"

    // 3. Độ hài lòng (Rating)
    private Double averageRating; // VD: 4.98
    private Double ratingGrowth; // VD: 0.02

    // 4. Tổng tài sản quản lý
    private Integer totalProperties; // VD: 24
}