package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.enums.kyc.KycDocumentStatus;
import clyvasync.Clyvasync.enums.kyc.KycProfileStatus;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@Data
@Builder
public class AdminHostResponse {
    private String id;
    private UserHeaderResponse user;
    private LocalDateTime joinDate;
    private String status;
    private VerificationInfo verification;
    private HostMetrics metrics;



    @Data
    @Builder
    public static class VerificationInfo {
        private String identityStatus;
        private String bankStatus;
    }

    @Data
    @Builder
    public static class HostMetrics {
        private int totalProperties;
        private int pendingProperties;
        private int totalBookings;
        private BigDecimal totalRevenue;
        private BigDecimal walletBalance;
        private double cancellationRate;
        private String averageResponseTime;
    }
}