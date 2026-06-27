package clyvasync.Clyvasync.dto.request;

import clyvasync.Clyvasync.enums.homestay.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DocumentUploadRequest {
    @NotNull(message = "Document type không được để trống")
    private DocumentType documentType;

    @NotBlank(message = "File URL không được để trống")
    private String fileUrl;
}