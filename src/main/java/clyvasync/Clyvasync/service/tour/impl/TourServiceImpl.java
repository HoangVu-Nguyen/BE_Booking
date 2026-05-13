package clyvasync.Clyvasync.service.tour.impl;

import clyvasync.Clyvasync.dto.request.CreateTourRequest;
import clyvasync.Clyvasync.dto.request.UpdateTourRequest;
import clyvasync.Clyvasync.dto.response.TourDetailResponse;
import clyvasync.Clyvasync.dto.response.TourResponse;
import clyvasync.Clyvasync.enums.type.TourStatus;
import clyvasync.Clyvasync.mapper.tour.TourMapper;
import clyvasync.Clyvasync.modules.tour.entity.Tour;
import clyvasync.Clyvasync.repository.tour.TourRepository;
import clyvasync.Clyvasync.service.tour.TourImageService;
import clyvasync.Clyvasync.service.tour.TourService;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class TourServiceImpl implements TourService {
    private final TourRepository tourRepository;
    private final TourImageService tourImageService;
    private final TourMapper tourMapper;

    @Override
    public TourResponse createTour(Long homestayId, CreateTourRequest request) {
        return null;
    }

    @Override
    public TourResponse updateTour(Long tourId, UpdateTourRequest request) {
        return null;
    }

    @Override
    public void deleteTour(Long tourId) {

    }

    @Override
    public void updateTourStatus(Long tourId, TourStatus status) {

    }

    @Override
    public TourDetailResponse getTourById(Long tourId) {
        return null;
    }

    @Override
    @Cacheable(value = "homestay_tours", key = "#homestayId", unless = "#result.isEmpty()")
    public List<TourResponse> getToursByHomestayId(Long homestayId) {
        log.info("Lấy danh sách tour cho homestay: {}", homestayId);

        List<Tour> tours = tourRepository.findAllByHomestayIdAndStatus(homestayId, TourStatus.AVAILABLE);
        if (tours.isEmpty()) return List.of();

        List<Long> tourIds = tours.stream().map(Tour::getId).toList();

        Map<Long, List<String>> imagesMap = tourImageService.getImagesForTours(tourIds);

        return tours.stream().map(entity -> {
            List<String> urls = imagesMap.getOrDefault(entity.getId(), List.of());

            String primary = !urls.isEmpty() ? urls.get(0) : null;
            String hover = urls.size() > 1 ? urls.get(1) : primary;

            return tourMapper.toResponse(entity, primary, hover);
        }).toList();
    }

    @Override
    public List<TourResponse> getExternalToursByHomestayId(Long homestayId) {
        return List.of();
    }

    @Override
    public Page<TourResponse> searchTours(String query, Long homestayId, TourStatus status, Pageable pageable) {
        return null;
    }

    @Override
    public Page<TourResponse> getAllTours(Pageable pageable) {
        return null;
    }
}

