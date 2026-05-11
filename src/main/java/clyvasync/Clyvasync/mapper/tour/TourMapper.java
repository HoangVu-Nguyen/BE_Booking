package clyvasync.Clyvasync.mapper.tour;

import clyvasync.Clyvasync.dto.response.TourResponse;
import clyvasync.Clyvasync.modules.tour.entity.Tour;
import clyvasync.Clyvasync.modules.tour.entity.TourImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.util.List;

@Mapper(componentModel = "spring")
public interface TourMapper {
    @Mapping(target = "primaryImageUrl", source = "images", qualifiedByName = "getPrimaryImage")
    @Mapping(target = "hoverImageUrl", source = "images", qualifiedByName = "getHoverImage") // Thêm dòng này
    TourResponse toResponse(Tour tour);

    @Named("getPrimaryImage")
    default String getPrimaryImage(List<TourImage> images) {
        if (images == null || images.isEmpty()) return null;
        return images.stream()
                .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                .map(TourImage::getImageUrl)
                .findFirst()
                .orElse(images.get(0).getImageUrl());
    }

    @Named("getHoverImage")
    default String getHoverImage(List<TourImage> images) {
        if (images == null || images.size() < 2) return null;

        return images.stream()
                .filter(img -> !Boolean.TRUE.equals(img.getIsPrimary()))
                .map(TourImage::getImageUrl)
                .findFirst()
                .orElse(images.get(1).getImageUrl());
    }    List<TourResponse> tourResponses(List<Tour> tours);
    Tour toEntity(TourResponse tourResponse);
    List<Tour> toEntity(List<TourResponse> tourResponses);
}
