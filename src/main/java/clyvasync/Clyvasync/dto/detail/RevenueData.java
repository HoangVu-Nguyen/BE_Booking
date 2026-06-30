package clyvasync.Clyvasync.dto.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RevenueData {
    private String label;  // Hiển thị dạng ngắn gọn (vd: "45M")
    private Double value;  // Giá trị thực để tính % chiều cao
    private String day;    // Nhãn trục X (vd: "T2", "T3" hoặc "W1", "W2")
    private boolean isToday; // Đánh dấu để highlight cột hiện tại
}