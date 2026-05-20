package clyvasync.Clyvasync.dto.projection;

import java.time.LocalDate;

public record BookingTimelineProjection(
        Long userId,
        Long roomId,
        Long bookingId,
        String guestName,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        String status
) {}