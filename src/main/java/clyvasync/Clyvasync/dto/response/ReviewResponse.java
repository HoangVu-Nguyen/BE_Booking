package clyvasync.Clyvasync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponse {
    private Long id;           // Từ reviews.id
    private Integer rating;    // Từ reviews.rating (1-5 sao)
    private String comment;    // Từ reviews.comment
    private LocalDateTime createdAt; // Từ reviews.created_at

    // Thông tin người đánh giá (Lấy từ bảng users qua user_id)
    private Long userId;
    private String fullName;   // Từ users.full_name (Để hiển thị tên khách hàng)
    private String avatarUrl;  // Từ user_photos (Nếu bạn muốn hiển thị ảnh đại diện khách)
    private List<String> imageUrls;
}