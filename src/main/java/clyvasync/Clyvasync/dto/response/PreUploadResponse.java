package clyvasync.Clyvasync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class PreUploadResponse {
    private Long documentId;
    private String fileName;
    private String objectKey;
    private String uploadUrl;
}
