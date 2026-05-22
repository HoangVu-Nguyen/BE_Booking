package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.enums.calendar.RoomCalendarStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@Builder
public class CalendarInventoryResponse {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;
    private BigDecimal priceOverride; // null nếu dùng giá gốc (basePrice)
    private Integer availableQuantity;
    private RoomCalendarStatus status; // Dùng Enum ở đây

    // Thêm Data cho Booking nếu status là BOOKED
    private String bookingCode;
    private String guestName;
}