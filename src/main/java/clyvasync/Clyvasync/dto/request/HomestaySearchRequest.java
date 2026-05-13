package clyvasync.Clyvasync.dto.request;

import java.math.BigDecimal;
import java.util.List;

public record HomestaySearchRequest(
        String city,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer guests,
        Double minRating,
        List<Integer> amenityIds,
        Long categoryId
) {}

