package clyvasync.Clyvasync.dto.projection;

import java.sql.Timestamp;

public interface ConversationInboxProjection {
    Long getConversationId();
    String getChatType();
    String getTargetName();
    String getTargetAvatar();
    String getLastMessageContent();
    Timestamp getLastMessageAt(); // Trả về Timestamp cho an toàn nhất với Native Query
    Long getUnreadCount();
    String getBookingStatus();
    String getPropertyName();
}