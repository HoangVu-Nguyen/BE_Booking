package clyvasync.Clyvasync.dto.projection;

import clyvasync.Clyvasync.enums.booking.BookingStatus;

import java.time.LocalDate;

public record BookingTimelineProjection(
        Long userId,
        Long roomId,
        Long bookingId,
        String guestName,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        BookingStatus status,
        Integer quantity
) {}