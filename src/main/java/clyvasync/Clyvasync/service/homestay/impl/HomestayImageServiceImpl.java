package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.dto.request.BatchUploadRequest;
import clyvasync.Clyvasync.dto.request.MultiRoomBatchUploadRequest;
import clyvasync.Clyvasync.dto.request.UploadRequest;
import clyvasync.Clyvasync.dto.response.PresignedUrlResponse;
import clyvasync.Clyvasync.enums.media.MediaStatus;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayImage;
import clyvasync.Clyvasync.repository.homestay.HomestayImageRepository;
import clyvasync.Clyvasync.service.homestay.HomestayImageService;
import clyvasync.Clyvasync.service.media.S3Service;
import clyvasync.Clyvasync.utils.MediaUtil;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Slf4j
public class HomestayImageServiceImpl implements HomestayImageService {
    private final HomestayImageRepository homestayImageRepository;
    private final S3Service s3Service;
    private final MediaUtil mediaUtil;


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

    @Override
    public List<HomestayImage> findByHomestayId(Long homestayId) {
        return homestayImageRepository.findByHomestayId(homestayId);
    }

    @Override
    public List<HomestayImage> findAllByIds(List<Long> homestayIds) {
        return homestayImageRepository.findAllByHomestayIdIn(homestayIds);
    }

    @Override
    public List<HomestayImage> saveAll(List<HomestayImage> homestayImages) {
        return homestayImageRepository.saveAll(homestayImages);
    }
    @Override
    @Transactional
    public List<PresignedUrlResponse> prepareHomestayImagesBatch(Long ownerId, BatchUploadRequest batchRequest) {
        Long homestayId = batchRequest.getTargetId();
        List<UploadRequest> items = batchRequest.getItems();

        if (CollectionUtils.isEmpty(items)) return List.of();

        List<PresignedUrlResponse> responses = new ArrayList<>(items.size());
        List<HomestayImage> pendingImages = new ArrayList<>();

        for (UploadRequest item : items) {
            String objectKey = mediaUtil.generateObjectKey(ownerId, item);
            String presignedUrl = s3Service.generatePresignedPutUrl(objectKey, item.getContentType(), item.getFileSize());

            HomestayImage image = HomestayImage.builder()
                    .homestayId(homestayId)
                    .imageUrl(objectKey)
                    .isPrimary(item.getIsCover() != null ? item.getIsCover() : false)
                    .displayOrder(item.getSortOrder() != null ? item.getSortOrder() : 0)
                    .status(MediaStatus.PENDING)
                    .build();
            pendingImages.add(image);

            responses.add(PresignedUrlResponse.builder()
                    .fileName(item.getFileName())
                    .uploadUrl(presignedUrl)
                    .objectKey(objectKey)
                    .build());
        }

        homestayImageRepository.saveAll(pendingImages);
        return responses;
    }

    @Override
    public List<HomestayImage> findByImageUrlIn(List<String> imageUrls) {
        return homestayImageRepository.findByImageUrlIn(imageUrls);
    }

    @Override
    public List<PresignedUrlResponse> prepareHomestayImageBatch(Long ownerId, MultiRoomBatchUploadRequest batchRequest) {
        return List.of();
    }
    @Override
    @CacheEvict(value = "homestay_images", key = "#homestayId")
    public void evictHomestayImagesCache(Long homestayId) {
        log.info("Xóa cache ảnh cho homestay: {}", homestayId);
    }
}
