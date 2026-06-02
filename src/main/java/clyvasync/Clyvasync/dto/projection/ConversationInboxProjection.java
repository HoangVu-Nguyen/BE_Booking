package clyvasync.Clyvasync.dto.projection;

import java.sql.Timestamp;
import java.time.Instant;

public interface ConversationInboxProjection {
    Long getConversationId();
    Long getTargetUserId();
    String getChatType();
    String getTargetName();
    String getTargetAvatar();
    String getLastMessageContent();
    Instant getLastMessageAt(); // Trả về Timestamp cho an toàn nhất với Native Query
    Long getUnreadCount();
    String getBookingStatus();
    String getPropertyName();
}