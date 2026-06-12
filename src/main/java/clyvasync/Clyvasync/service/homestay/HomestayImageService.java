package clyvasync.Clyvasync.service.homestay;

import clyvasync.Clyvasync.dto.request.BatchUploadRequest;
import clyvasync.Clyvasync.dto.response.PresignedUrlResponse;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayImage;

import java.util.List;
import java.util.Map;

public interface HomestayImageService {
    Map<Long, List<String>> getImagesForHomestays(List<Long> homestayIds);
    List<String> getImagesByHomestayId(Long homestayId);
    List<HomestayImage> findByHomestayId(Long homestayId);
    List<HomestayImage> findAllByIds(List<Long> homestayIds);
    List<HomestayImage> saveAll(List<HomestayImage> homestayImages);
    List<PresignedUrlResponse> prepareHomestayImagesBatch(Long ownerId, BatchUploadRequest batchRequest);

}
