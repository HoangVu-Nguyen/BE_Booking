package clyvasync.Clyvasync.dto.request;

import lombok.Data;
import org.springframework.ai.tool.annotation.ToolParam;

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
        List<Integer> amenityIds,
        String checkInDate,

        String checkOutDate,
        Boolean allowPets,

        Boolean allowSmoking



) {
    public GlobalSearchRequest(String keyword, String category, BigDecimal minPrice, BigDecimal maxPrice, Integer guests, Integer bedrooms, Double minRating, List<Integer> amenityIds) {
        this(keyword, category, minPrice, maxPrice, guests, bedrooms, minRating, amenityIds, "", "", null, null);
    }
}