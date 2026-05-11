package clyvasync.Clyvasync.service.tour.impl;

import clyvasync.Clyvasync.dto.response.TourImageResponse;
import clyvasync.Clyvasync.service.tour.TourImageService;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class TourImageServiceImpl implements TourImageService {
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
}
