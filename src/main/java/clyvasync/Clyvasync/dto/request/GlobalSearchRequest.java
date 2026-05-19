package clyvasync.Clyvasync.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

public record GlobalSearchRequest(
        String keyword,
        String category, // "ALL", "HOMESTAY", "TOUR"
        BigDecimal minPrice,
        BigDecimal maxPrice,
        Integer guests,
        Integer bedrooms,
        Double minRating,
        List<Integer> amenityIds
) {}