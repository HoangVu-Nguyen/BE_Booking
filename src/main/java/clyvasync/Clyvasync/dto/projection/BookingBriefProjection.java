package clyvasync.Clyvasync.dto.projection;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface BookingBriefProjection {
    Long getBookingId();
    String getBookingCode();
    String getHomestayName();
    String getStatus();
    LocalDate getCheckIn();
    LocalDate getCheckOut();
    BigDecimal getTotalPrice();
}