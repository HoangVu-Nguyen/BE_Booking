package clyvasync.Clyvasync.mapper.homestay;

import clyvasync.Clyvasync.dto.response.AmenityResponse;
import clyvasync.Clyvasync.modules.homestay.entity.Amenity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface AmenityMapper {
    @Mapping(target = "groupName", source = "amenity.groupName")
    AmenityResponse toAmenityResponse(Amenity amenity);
    List<AmenityResponse> toAmenityResponseList(List<Amenity> amenityList);
}
