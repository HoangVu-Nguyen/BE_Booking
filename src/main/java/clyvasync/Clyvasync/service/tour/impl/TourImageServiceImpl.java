package clyvasync.Clyvasync.service.tour.impl;

import clyvasync.Clyvasync.dto.response.TourImageResponse;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.tour.entity.TourImage;
import clyvasync.Clyvasync.repository.tour.TourImageRepository;
import clyvasync.Clyvasync.service.tour.TourImageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class TourImageServiceImpl implements TourImageService {
    private final TourImageRepository tourImageRepository;
    private final clyvasync.Clyvasync.service.media.S3Service s3Service;
    private final clyvasync.Clyvasync.utils.MediaUtil mediaUtil;

    @Override
    public List<TourImageResponse> uploadImages(Long tourId, List<MultipartFile> files) {
        return List.of();
    }

    @Override
    public void deleteImage(Long imageId) {

    }

    @Override
    public void setPrimaryImage(Long tourId, Long imageId) {

    }

    @Override
    public List<TourImageResponse> getImagesByTourId(Long tourId) {
        return List.of();
    }
    @Override
    public Map<Long, List<String>> getImagesForTours(List<Long> tourIds) {
        if (tourIds == null || tourIds.isEmpty()) return Map.of();

        List<TourImage> images = tourImageRepository.findImagesForHover(tourIds);

        return images.stream().collect(Collectors.groupingBy(
                TourImage::getTourId,
                Collectors.mapping(TourImage::getImageUrl, Collectors.toList())
        ));
    }

    @Override
    public TourImage getPrimaryImageUrl(Long tourId) {
        return tourImageRepository.findFirstByTourIdAndIsPrimaryTrue(tourId).orElseThrow(()->new AppException(ResultCode.TOUR_IMAGE_NOT_FOUND));
    }

    @Override
    public Map<Long, String> getPrimaryImagesByTourIds(List<Long> tourIds) {
        return tourImageRepository.getPrimaryImagesByTourIds(tourIds);
    }

    @Override
    public List<TourImage> findByTourIdIn(List<Long> tourIds) {
        return tourImageRepository.findByTourIdIn(tourIds);
    }

    @Override
    public List<clyvasync.Clyvasync.dto.response.PresignedUrlResponse> prepareTourImagesBatch(Long ownerId, clyvasync.Clyvasync.dto.request.BatchUploadRequest batchRequest) {
        java.util.List<clyvasync.Clyvasync.dto.request.UploadRequest> items = batchRequest.getItems();
        if (items == null || items.isEmpty()) {
            throw new AppException(ResultCode.INVALID_INPUT);
        }

        java.util.List<clyvasync.Clyvasync.dto.response.PresignedUrlResponse> responses = new java.util.ArrayList<>();
        java.util.List<TourImage> pendingImages = new java.util.ArrayList<>();

        for (clyvasync.Clyvasync.dto.request.UploadRequest item : items) {
            String objectKey = mediaUtil.generateObjectKey(ownerId, item);
            String presignedUrl = s3Service.generatePresignedPutUrl(objectKey, item.getContentType(), item.getFileSize());

            TourImage image = new TourImage();
            image.setOwnerId(ownerId);
            image.setImageUrl(objectKey);
            image.setIsPrimary(item.getIsCover() != null ? item.getIsCover() : false);
            image.setDisplayOrder(item.getSortOrder() != null ? item.getSortOrder() : 0);
            image.setStatus(clyvasync.Clyvasync.enums.media.MediaStatus.PENDING);
            pendingImages.add(image);

            responses.add(clyvasync.Clyvasync.dto.response.PresignedUrlResponse.builder()
                    .fileName(item.getFileName())
                    .uploadUrl(presignedUrl)
                    .objectKey(objectKey)
                    .build());
        }

        tourImageRepository.saveAll(pendingImages);
        return responses;
    }
}
