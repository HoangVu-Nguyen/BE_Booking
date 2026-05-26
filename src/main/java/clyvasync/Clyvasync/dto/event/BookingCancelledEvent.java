package clyvasync.Clyvasync.dto.event;


import clyvasync.Clyvasync.dto.response.CancelPreviewResponse;
import clyvasync.Clyvasync.enums.booking.BookingStatus;
import clyvasync.Clyvasync.modules.booking.entity.Booking;

public record BookingCancelledEvent(
        Booking booking,
        BookingStatus previousStatus,
        String actorType,
        CancelPreviewResponse cancelPreviewResponse
) {}
