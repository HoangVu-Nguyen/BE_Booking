package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.dto.request.BatchUploadRequest;
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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        List<UploadRequest> items = batchRequest.getItems();
        List<PresignedUrlResponse> responses = new ArrayList<>(items.size());
        List<HomestayImage> pendingImages = new ArrayList<>();

        for (UploadRequest item : items) {
            String objectKey = MediaUtil.generateObjectKey(ownerId,item);

            // 2. Lấy URL từ S3
            String presignedUrl = s3Service.generatePresignedPutUrl(objectKey, item.getContentType(), item.getFileSize());

            // 3. Tạo bản ghi PENDING (Chưa gắn homestayId)
            HomestayImage image = HomestayImage.builder()
                    .homestayId(null)
                    .imageUrl(objectKey)
                    .isPrimary(pendingImages.isEmpty())
                    .status(MediaStatus.PENDING)
                    .build();
            pendingImages.add(image);

            responses.add(PresignedUrlResponse.builder()
                    .uploadUrl(presignedUrl)
                    .objectKey(objectKey)
                    .build());
        }

        homestayImageRepository.saveAll(pendingImages);
        return responses;
    }
}
