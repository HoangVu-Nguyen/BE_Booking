package clyvasync.Clyvasync.service.tour.impl;

import clyvasync.Clyvasync.dto.request.CreateTourRequest;
import clyvasync.Clyvasync.dto.request.UpdateTourRequest;
import clyvasync.Clyvasync.dto.response.TourDetailResponse;
import clyvasync.Clyvasync.dto.response.TourResponse;
import clyvasync.Clyvasync.enums.type.TourStatus;
import clyvasync.Clyvasync.mapper.tour.TourMapper;
import clyvasync.Clyvasync.modules.tour.entity.Tour;
import clyvasync.Clyvasync.repository.tour.TourRepository;
import clyvasync.Clyvasync.service.tour.TourService;
import com.alicp.jetcache.anno.CacheInvalidate;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.checkerframework.checker.units.qual.A;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
@AllArgsConstructor
public class TourServiceImpl implements TourService {
    private final TourRepository tourRepository;
    private final TourMapper tourMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    @CacheInvalidate(name = "tour:homestay:", key = "#homestayId")
    public TourResponse createTour(Long homestayId, CreateTourRequest request) {
        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public TourResponse updateTour(Long tourId, UpdateTourRequest request) {

        return null;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
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
    @Transactional(readOnly = true)
   @Cached(name = "tour:homestay:", key = "#homestayId", expire = 3600, cacheType = CacheType.REMOTE)
    public List<TourResponse> getToursByHomestayId(Long homestayId) {
        log.info("Fetching tours for homestayId: {}", homestayId);
        List<Tour> tours = tourRepository.findByHomestayId(homestayId);
        return tours.stream()
                .map(tourMapper::toResponse)
                .toList();
    }
    @Override
    @Transactional(readOnly = true)
    @Cached(name = "tour:external:", key = "#homestayId", expire = 3600)
    public List<TourResponse> getExternalToursByHomestayId(Long homestayId) {
        log.info("Fetching external tours for homestay: {}", homestayId);
        return tourRepository.findExternalTours(homestayId).stream()
                .map(tourMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TourResponse> searchTours(String query, Long homestayId, TourStatus status, Pageable pageable) {
        log.info("Searching tours with query: {}", query);

        // Mặc định lấy tour ACTIVE nếu không truyền status
        TourStatus searchStatus = (status != null) ? status : TourStatus.ACTIVE;

        return tourRepository.searchTours(query, homestayId, searchStatus, pageable)
                .map(tourMapper::toResponse);
    }
    @Override
    @Transactional(readOnly = true)
   // @Cached(name = "tour:all:", key = "#pageable.pageNumber + '-' + #pageable.pageSize", expire = 600)
    public Page<TourResponse> getAllTours(Pageable pageable) {
        log.info("Fetching all tours - Page: {}, Size: {}", pageable.getPageNumber(), pageable.getPageSize());

        return tourRepository.findAllTours(pageable)
                .map(tourMapper::toResponse);
    }
}

