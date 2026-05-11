package clyvasync.Clyvasync.dto.response;

public record TourImageResponse(
        Long id,
        String imageUrl,
        Boolean isPrimary,
        Integer displayOrder
) {}