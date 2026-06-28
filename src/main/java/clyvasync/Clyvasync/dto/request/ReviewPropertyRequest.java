package dto.request;

import lombok.Data;
import java.util.List;

@Data
public class ReviewPropertyRequest {
    private List<DocumentReviewItem> documents;

    @Data
    public static class DocumentReviewItem {
        private Long documentId;
        private clyvasync.Clyvasync.enums.homestay.DocumentStatus status;
        private String rejectReason;
    }
}