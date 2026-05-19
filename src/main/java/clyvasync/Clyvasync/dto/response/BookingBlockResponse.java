package clyvasync.Clyvasync.dto.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record BookingBlockResponse(
        Long bookingId,
        String guestName,
        LocalDate checkInDate,
        LocalDate checkOutDate,
        String status
) {}