package clyvasync.Clyvasync.dto.request;

import lombok.Data;

@Data
public class ImageRequest {
    private Long id;
    private String objectKey;
    private Boolean isCover;
    private Boolean isNew; // Quan trọng: Để biết có cần upload file mới không
    private Integer sortOrder;
}