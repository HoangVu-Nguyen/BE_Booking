package clyvasync.Clyvasync.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NewImageRequest {

    // Tên file gốc từ máy tính của Host (Ví dụ: "phong-ngu-1.jpg")
    // Dùng để Backend lấy phần mở rộng (.jpg) tạo S3 Key,
    // và để Frontend map được link upload trả về với đúng file đang cầm trên RAM.
    private String fileName;

    // Có được set làm ảnh bìa hay không
    private Boolean isCover;

    // Thứ tự hiển thị
    private Integer sortOrder;
}