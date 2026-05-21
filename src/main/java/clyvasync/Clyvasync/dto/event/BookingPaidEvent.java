package clyvasync.Clyvasync.dto.event;

import clyvasync.Clyvasync.modules.booking.entity.Booking;

public record BookingPaidEvent(Booking booking) {
}