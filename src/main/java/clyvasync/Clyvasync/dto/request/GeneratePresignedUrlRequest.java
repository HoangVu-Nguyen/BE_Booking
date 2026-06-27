package clyvasync.Clyvasync.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GeneratePresignedUrlRequest {
    @NotBlank(message = "Object key không được để trống")
    private String objectKey;

    private String action;
}
