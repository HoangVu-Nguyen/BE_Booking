package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.modules.homestay.entity.HomestayImage;
import clyvasync.Clyvasync.repository.homestay.HomestayImageRepository;
import clyvasync.Clyvasync.service.homestay.HomestayImageService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class HomestayImageServiceImpl implements HomestayImageService {
    private final HomestayImageRepository homestayImageRepository;


    @Override
    public Map<Long, List<String>> getImagesForHomestays(List<Long> homestayIds) {
        if (homestayIds == null || homestayIds.isEmpty()) return Map.of();

        List<HomestayImage> allImages = homestayImageRepository.findByHomestayIdInOrderByDisplayOrderAsc(homestayIds);

        return allImages.stream().collect(Collectors.groupingBy(
                HomestayImage::getHomestayId,
                Collectors.mapping(HomestayImage::getImageUrl, Collectors.toList())
        ));
    }
    @Cacheable(value = "homestay_images", key = "#homestayId")
    public List<String> getImagesByHomestayId(Long homestayId) {
        log.info("Lấy ảnh từ DB cho homestay đơn lẻ: {}", homestayId);
        return homestayImageRepository.findByHomestayIdOrderByDisplayOrderAsc(homestayId)
                .stream().map(HomestayImage::getImageUrl).toList();
    }
}
