package clyvasync.Clyvasync.mapper.homestay;

import clyvasync.Clyvasync.dto.request.HomestayRequest;
import clyvasync.Clyvasync.dto.response.HomestayResponse;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface HomestayMapper {

    @Mapping(target = "cityName", ignore = true)
    @Mapping(target = "categoryName", ignore = true)
    @Mapping(target = "amenities", ignore = true)
    @Mapping(target = "owner", ignore = true)
    // TRÁNH LỖI GENERIC: Chỉ định rõ hàm map cho List Images
    @Mapping(target = "imageUrls", ignore = true)
    HomestayResponse toResponse(Homestay homestay);

}