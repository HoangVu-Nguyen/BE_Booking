package clyvasync.Clyvasync.dto.record;

import java.util.List;

public record AiSearchRequest(
        String location,
        String homestayName,
        Integer guests,
        Integer  bedCount,
        Double minPrice,
        Double maxPrice,
        String checkInDate,
        String checkOutDate,

        List<Integer> amenityIds,
        PolicyFilterRequest policyFilter,
        String semanticQuery
) {}