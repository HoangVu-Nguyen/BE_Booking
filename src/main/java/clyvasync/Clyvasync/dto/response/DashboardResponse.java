package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.dto.detail.RevenueData;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class DashboardResponse {
    private Double gmvToday;
    private Double gmvGrowthPercentage;
    private Long newBookings;
    private Long pendingBookings;
    private Long newHosts;
    private Long pendingKycCount;
    private Double occupancyRate;
    private List<RevenueData> revenueChart;
}