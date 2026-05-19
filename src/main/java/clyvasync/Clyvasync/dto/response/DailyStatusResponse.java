package clyvasync.Clyvasync.dto.response;

import lombok.Builder;

import java.time.LocalDate;

@Builder
public record DailyStatusResponse(
        LocalDate date,
        java.math.BigDecimal price, // Giá sau khi đã override (nếu có)
        Integer availableQuantity
) {}