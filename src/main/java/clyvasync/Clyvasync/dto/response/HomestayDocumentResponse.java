package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.enums.homestay.DocumentStatus;
import clyvasync.Clyvasync.enums.homestay.DocumentType;
import clyvasync.Clyvasync.enums.homestay.PropertyDocumentType;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HomestayDocumentResponse {
    private Long id;
    private PropertyDocumentType documentType;
    private String fileUrl;
    private String viewUrl;
    private DocumentStatus status;
    private String rejectionReason;
    private LocalDateTime uploadedAt;
}