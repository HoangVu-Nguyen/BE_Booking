package clyvasync.Clyvasync.dto.response;


import clyvasync.Clyvasync.enums.type.PaymentStatus;
import clyvasync.Clyvasync.enums.type.TourBookingStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public record TourBookingResponse(
        Long id,
        String bookingCode, // VD: T-20260511-XYZ

        // Thông tin cơ bản của Tour để hiển thị lên UI không cần gọi thêm API
        Long tourId,
        String tourName,

        Long userId,
        Long homestayBookingId, // Trả về để UI biết booking này có gắn với phòng không

        LocalDate tourDate,
        Integer participantCount,
        BigDecimal totalPrice,

        TourBookingStatus status,
        PaymentStatus paymentStatus,
        String cancellationReason,

        Instant createdAt
) {}