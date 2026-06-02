package clyvasync.Clyvasync.dto.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class BookingContextInfo {
    private String bookingCode;      // Mã booking gốc
    private String homestayName;     // Tên homestay
    private String status;           // PENDING, CONFIRMED...
    private String checkIn;          // Ngày vào
    private String checkOut;         // Ngày ra
    private BigDecimal totalPrice;   // Giá tổng (Homestay + Tour)
    private List<TourInfo> bookedTours; // Danh sách tour đi kèm nếu có
}