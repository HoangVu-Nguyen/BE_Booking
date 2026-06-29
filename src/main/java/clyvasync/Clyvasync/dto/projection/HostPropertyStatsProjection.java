package clyvasync.Clyvasync.dto.projection;

public interface HostPropertyStatsProjection {
    Long getOwnerId();
    Integer getTotalProperties();
    Integer getPendingProperties(); // Số lượng nhà có giấy tờ đang chờ duyệt
}