package clyvasync.Clyvasync.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExistingImageRequest {

    // ID của ảnh trong database (Bắt buộc phải có)
    private Long id;

    // Đánh dấu ảnh này có được set làm ảnh cover mới hay không
    private Boolean isCover;

    // Thứ tự sắp xếp của ảnh (nếu Host kéo thả đổi vị trí)
    private Integer sortOrder;
}