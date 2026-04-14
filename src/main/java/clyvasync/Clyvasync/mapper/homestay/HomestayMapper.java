package clyvasync.Clyvasync.mapper.homestay;

import clyvasync.Clyvasync.dto.request.HomestayRequest;
import clyvasync.Clyvasync.dto.response.HomestayResponse;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayImage;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import java.util.List;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface HomestayMapper {

    // Tạo mới
    @Mapping(target = "images", source = "imageUrls")
    Homestay toHomestay(HomestayRequest request);

    // Cập nhật (Ghi đè dữ liệu request lên entity có sẵn)
    @Mapping(target = "images", source = "imageUrls")
    @Mapping(target = "id", ignore = true) // Không bao giờ cho phép update ID
    @Mapping(target = "ownerId", ignore = true) // Không đổi chủ
    @Mapping(target = "averageRating", ignore = true) // Không cho tự fake rating
    @Mapping(target = "reviewCount", ignore = true)
    @Mapping(target = "version", ignore = true) // Để Hibernate tự lo Optimistic Lock
    void updateHomestayFromRequest(HomestayRequest request, @MappingTarget Homestay homestay);

    // Trả về Client
    @Mapping(target = "images", source = "images")
    HomestayResponse toHomestayResponse(Homestay homestay);

    // --- CÁC HÀM DEFAULT ĐỂ MAPSTRUCT TỰ ĐỘNG GỌI ---

    // Chuyển List<HomestayImage> (Entity) thành List<String> (URL) cho Response
    default List<String> mapImagesToUrls(List<HomestayImage> images) {
        if (images == null) return null;
        return images.stream().map(HomestayImage::getImageUrl).collect(Collectors.toList());
    }

    // Chuyển List<String> (URL) từ Request thành List<HomestayImage> (Entity)
    default List<HomestayImage> mapUrlsToImages(List<String> urls) {
        if (urls == null) return null;
        return urls.stream().map(url -> {
            HomestayImage img = new HomestayImage();
            img.setImageUrl(url);
            return img;
        }).collect(Collectors.toList());
    }
}