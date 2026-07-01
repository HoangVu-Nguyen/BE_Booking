package clyvasync.Clyvasync.dto.projection;

import java.math.BigDecimal;

public interface MonthlyRevenueProjection {
    Integer getMonth();
    BigDecimal getRevenue();
}
