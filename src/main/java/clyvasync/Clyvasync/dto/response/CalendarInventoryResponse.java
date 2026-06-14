package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.dto.detail.BookingSimpleInfo;
import clyvasync.Clyvasync.enums.calendar.RoomCalendarStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
public class CalendarInventoryResponse {

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;

    // Giá đại diện để FE hiển thị trên ô lịch
    private BigDecimal displayPrice;

    // Chỉ có giá trị khi ngày đó có custom price
    private BigDecimal priceOverride;

    // Có ít nhất một rate plan bị override giá hay không
    private Boolean hasPriceOverride;

    private Integer availableQuantity;

    private RoomCalendarStatus status;

    private String bookingCode;
    private String guestName;

    private Integer totalBookedInDay;

    private List<BookingSimpleInfo> bookings;

    private List<RatePlanPriceResponse> ratePlanPrices;
}