package clyvasync.Clyvasync.service.tour.impl;

import clyvasync.Clyvasync.dto.response.TourImageResponse;
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
}
