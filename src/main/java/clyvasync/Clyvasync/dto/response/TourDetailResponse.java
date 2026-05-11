package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.enums.type.DurationType;
import clyvasync.Clyvasync.enums.type.TourStatus;

import java.math.BigDecimal;
import java.util.List;

public record TourDetailResponse(
        Long id,
        Long homestayId,
        String name,
        String description,
        DurationType durationType,
        Integer durationValue,
        BigDecimal pricePerPerson,
        Integer maxParticipants,
        Boolean allowExternalGuests,
        TourStatus status,
        List<TourImageResponse> images // Trả về toàn bộ danh sách ảnh
) {}
