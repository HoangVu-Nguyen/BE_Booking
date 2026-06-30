package clyvasync.Clyvasync.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class HostOverviewMetricsResponse {
    private long totalHosts;
    private long pendingKycHosts;
    private long totalProperties;
    private long suspendedHosts;
}