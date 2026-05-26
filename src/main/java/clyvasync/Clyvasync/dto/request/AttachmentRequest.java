package clyvasync.Clyvasync.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public  class AttachmentRequest {
    @NotNull
    private String fileUrl;

    @NotNull
    private String fileType; // VD: "image/jpeg", "application/pdf"
}