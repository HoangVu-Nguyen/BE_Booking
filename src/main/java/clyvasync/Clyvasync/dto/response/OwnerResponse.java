package clyvasync.Clyvasync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OwnerResponse {
    private Long id;              // ID để định danh chủ nhà
    private String fullName;      // Tên hiển thị (Ví dụ: "Nguyễn Văn A")
    private String avatar;        // Ảnh đại diện (Rất quan trọng để tạo lòng tin)
    private String phoneNumber;   // Số điện thoại (Chỉ hiện khi đã đặt phòng xong, hoặc ẩn bớt)
    private LocalDateTime joinedAt; // "Tham gia từ tháng 5, 2024" -> Tạo độ uy tín
    private Boolean isVerified;    // Tích xanh chủ nhà (nếu bác có tính năng này)
}