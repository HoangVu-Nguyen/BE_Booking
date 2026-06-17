package clyvasync.Clyvasync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HomestayImageResponse {
    private Long id;
    private String imageUrl;
    private String objectKey;    // key thật trong S3/DB
    private Boolean isCover;
    private Integer displayOrder;
}
