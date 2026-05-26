package clyvasync.Clyvasync.dto.request;

import clyvasync.Clyvasync.enums.type.MessageType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class SendMessageRequest {
    private String content;

    @NotNull(message = "Loại tin nhắn không được để trống (TEXT, IMAGE, SYSTEM)")
    private MessageType type;

    private List<AttachmentRequest> attachments;


}