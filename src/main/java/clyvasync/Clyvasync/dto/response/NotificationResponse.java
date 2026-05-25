package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.enums.type.NotificationType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponse {

    private Long id;

    private NotificationType type;


    private String category;

    private String title;

    private String message;

    private boolean isRead;

    private OffsetDateTime createdAt;

    private String timeAgo;

    private Map<String, Object> metadata;
}