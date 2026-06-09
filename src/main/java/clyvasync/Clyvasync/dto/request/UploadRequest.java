package clyvasync.Clyvasync.dto.request;

import clyvasync.Clyvasync.enums.media.ImageType;
import lombok.Data;

@Data
public class UploadRequest {
    private String fileName;    // Ví dụ: my-photo.jpg
    private String contentType; // Ví dụ: image/jpeg hoặc image/png
    private ImageType imageType;   // Dùng để phân loại: AVATAR, COVER, hoặc POST
    private Long fileSize;
}