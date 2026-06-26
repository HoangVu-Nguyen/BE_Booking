package clyvasync.Clyvasync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PreUploadResponse {
    private Long documentId;
    private String fileName;
    private String objectKey;
    private String uploadUrl;
}
