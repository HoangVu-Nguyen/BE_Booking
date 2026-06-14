package clyvasync.Clyvasync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class PresignedUrlResponse {
    private Long roomId;
    private String fileName;
    private String objectKey;
    private String uploadUrl;
}
