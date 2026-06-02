package clyvasync.Clyvasync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class HostInboxSummaryResponse {
    private Long conversationId;
    private Long guestId;
    private String guestName;
    private String guestAvatar;
    private String lastMessage;
    private String lastMessageTime;
    private Integer unreadCount;
    private String bookingStatus;
}
