package clyvasync.Clyvasync.dto.projection;

public interface HostKycStatsProjection {
    Long getUserId();
    String getKycStatus();
    Integer getPendingKycDocs();
}