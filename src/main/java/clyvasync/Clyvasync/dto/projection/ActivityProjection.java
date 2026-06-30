package clyvasync.Clyvasync.dto.projection;

import java.time.LocalDateTime;

public interface ActivityProjection {
    String getId();
    String getTitle();
    LocalDateTime getTime();
    String getType();
    String getStatus();
}