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
    @Mapping(target = "primaryImageUrl", source = "primary")
    @Mapping(target = "hoverImageUrl", source = "hover")
    @Mapping(target = "id", source = "entity.id")
    @Mapping(target = "name", source = "entity.name")
    TourResponse toResponse(Tour entity, String primary, String hover);

}
