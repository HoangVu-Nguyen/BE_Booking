package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.dto.detail.BookingContextInfo;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ChatInitResponse {
    private Long conversationId;
    private String name;
    private String avatar;
    private BookingContextInfo booking;
}