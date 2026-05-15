package clyvasync.Clyvasync.service.tour;

import clyvasync.Clyvasync.dto.response.TourImageResponse;
import clyvasync.Clyvasync.modules.tour.entity.TourImage;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

public interface TourImageService {
    List<TourImageResponse> uploadImages(Long tourId, List<MultipartFile> files);

    /** Xóa 1 ảnh cụ thể */
    void deleteImage(Long imageId);

    /** Set một ảnh làm ảnh đại diện (Thumbnail) cho Tour */
    void setPrimaryImage(Long tourId, Long imageId);

    /** Lấy danh sách ảnh của 1 Tour */
    List<TourImageResponse> getImagesByTourId(Long tourId);
    Map<Long, List<String>> getImagesForTours(List<Long> tourIds);
    TourImage getPrimaryImageUrl(Long tourId);
    Map<Long, String> getPrimaryImagesByTourIds(List<Long> tourIds);

}
