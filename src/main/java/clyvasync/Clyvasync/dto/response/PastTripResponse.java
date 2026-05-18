package clyvasync.Clyvasync.dto.response;


import lombok.*;
import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PastTripResponse {
    private Long bookingId;
    private String bookingCode;
    private String homestayName;
    private String primaryImageUrl;
    private String locationName; // Ví dụ: "Đà Lạt, Lâm Đồng" hoặc "Ninh Thuận"
    private String completedMonthYear; // Định dạng hiển thị FE: "Tháng 10, 2025"
    private BigDecimal averageRating;
    private String tripReviewStatus; // Dùng để check xem đơn này khách đã viết review chưa: "REVIEWED" | "NOT_REVIEWED"
}
