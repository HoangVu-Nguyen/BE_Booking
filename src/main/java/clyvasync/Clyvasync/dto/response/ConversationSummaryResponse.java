package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.enums.type.ChatType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConversationSummaryResponse {
    private Long id;
    private ChatType type; // Lấy từ conversations.type
    private Long targetUserId;
    private String targetName;
    private String targetAvatar;
    private String lastMessage; // Lấy từ messages.content của tin mới nhất

    private OffsetDateTime rawLastMessageAt; // Từ conversations.last_message_at
    private String lastMessageTime;

    private Long unreadCount; // Tính qua conversation_participants.last_read_message_id

    private String bookingStatus;
    private String propertyName;


}