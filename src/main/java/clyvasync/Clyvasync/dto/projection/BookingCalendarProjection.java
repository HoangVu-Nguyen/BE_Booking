package clyvasync.Clyvasync.dto.projection;

import java.time.LocalDate;

public interface BookingCalendarProjection {
    Long getRoomId();
    LocalDate getCheckInDate();
    LocalDate getCheckOutDate();
    Integer getQuantity();
    String getBookingCode();
    String getGuestName();
}
