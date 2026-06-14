package clyvasync.Clyvasync.dto.request;

import lombok.Data;

@Data
public class ImageSubmitRequest {
    private Long id;          // Có giá trị = Ảnh cũ
    private String objectKey; // Có giá trị = Ảnh mới
    private Boolean isCover;
    private Integer sortOrder;
}