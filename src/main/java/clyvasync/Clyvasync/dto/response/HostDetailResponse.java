package clyvasync.Clyvasync.dto.response;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@ToString
public class HostDetailResponse {
    private HostInfo host;
    private List<PropertyDto> properties;
    private List<AuditLogDto> auditLogs;

    @Data
    @Builder
    public static class HostInfo {
        private String id;
        private String joinDate;
        private String status;
        private BigDecimal walletBalance;
        private BigDecimal totalRevenue;
        private UserHeaderResponse user;
        private MetricsDto metrics;
        private KycDto kyc;
    }



    @Data
    @Builder
    public static class MetricsDto {
        private int totalBookings;
        private double cancellationRate;
        private double responseRate;
        private double avgRating;
        private int reviewsCount;
    }

    @Data
    @Builder
    public static class KycDto {
        private String identity;
        private String idNumber;
        private BankInfoDto bankInfo;
        private String frontImageUrl;
        private String backImageUrl;

    }

    @Data
    @Builder
    public static class BankInfoDto {
        private String bankName;
        private String accountNo;
        private String ownerName;
    }

    @Data
    @Builder
    public static class PropertyDto {
        private String id;
        private String name;
        private String type;
        private String location;
        private String image;
        private String status;
        private PropertyMetricsDto metrics;
    }

    @Data
    @Builder
    public static class PropertyMetricsDto {
        private int bookings;
        private BigDecimal revenue;
        private double rating;
    }

    @Data
    @Builder
    public static class AuditLogDto {
        private String time;
        private String action;
        private String desc;
        private String status;
    }
}
