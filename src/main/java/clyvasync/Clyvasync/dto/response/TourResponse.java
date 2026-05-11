package clyvasync.Clyvasync.dto.response;


import clyvasync.Clyvasync.enums.type.DurationType;
import clyvasync.Clyvasync.enums.type.TourStatus;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

public record TourResponse(
        Long id,
        Long homestayId,
        String name,
        DurationType durationType,
        Integer durationValue,
        BigDecimal pricePerPerson,
        Integer maxParticipants,
        TourStatus status,
        String primaryImageUrl,
        String hoverImageUrl
) implements Serializable {}