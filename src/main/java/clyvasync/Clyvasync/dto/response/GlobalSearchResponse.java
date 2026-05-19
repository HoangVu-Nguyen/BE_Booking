package clyvasync.Clyvasync.dto.response;

import java.math.BigDecimal;
import java.util.List;

public record GlobalSearchResponse(
        Long id,
        String name,
        String cityName,
        BigDecimal basePrice,
        List<String> imageUrls,
        String type, // "HOMESTAY" hoặc "TOUR"
        Double rating,
        Integer guests,
        Integer bedrooms
) {}