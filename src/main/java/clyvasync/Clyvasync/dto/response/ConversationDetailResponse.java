package clyvasync.Clyvasync.dto.response;


import clyvasync.Clyvasync.enums.type.ChatType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class ConversationDetailResponse {
    private Long id;
    private String guestName;
    private String guestAvatar;
    private String lastMessage;
    private String lastMessageTime;
    private long unreadCount;
    private String status; // PRE_ARRIVAL, IN_HOUSE, POST_DEPARTURE, CANCELLED
    private BookingDetailsResponse booking; // Đối tượng lồng nhau
    private List<MessageResponse> messages;

}