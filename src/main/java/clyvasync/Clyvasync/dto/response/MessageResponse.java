package clyvasync.Clyvasync.dto.response;


import clyvasync.Clyvasync.enums.type.MessageType;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MessageResponse {
    private Long id;

    private Long senderId;

    private String senderName;

    private String senderAvatar;

    private String content;

    private MessageType type; // TEXT, IMAGE, SYSTEM

    private String time; // VD: "10:30"

    private boolean isMine;

    private List<AttachmentResponse> attachments;
}