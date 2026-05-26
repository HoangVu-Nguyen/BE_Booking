package clyvasync.Clyvasync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AttachmentResponse {
    private Long id;
    private String fileUrl;
    private String fileType;
}
