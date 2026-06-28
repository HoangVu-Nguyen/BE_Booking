package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.enums.homestay.DocumentStatus;
import clyvasync.Clyvasync.enums.homestay.PropertyDocumentType;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Data
@Builder
public class PendingPropertyResponse {
    private String id;
    private Long profileId;
    private String homestayName;
    private String hostName;
    private List<DocumentDto> documents;
    private OffsetDateTime submittedAt;

    @Data
    @Builder
    public static class DocumentDto {
        private Long id;
        private String name;
        private String url;
        private DocumentStatus status;
    }
}