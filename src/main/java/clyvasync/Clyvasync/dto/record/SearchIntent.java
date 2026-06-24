package clyvasync.Clyvasync.dto.record;

import java.math.BigDecimal;
import java.util.List;

public record SearchIntent(
        String semanticQuery,
        List<Integer> mustHaveAmenityIds,
        List<Integer> mustNotHaveAmenityIds,
        Integer maxGuests,
        Integer minBeds,
        BigDecimal targetPrice,
        BigDecimal maxPrice,
        BigDecimal minPrice,
        String city,
        String sortBy
) {}