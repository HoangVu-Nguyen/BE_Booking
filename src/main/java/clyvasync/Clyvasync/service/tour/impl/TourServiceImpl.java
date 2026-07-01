package clyvasync.Clyvasync.service.tour.impl;

import clyvasync.Clyvasync.dto.request.CreateTourRequest;
import clyvasync.Clyvasync.dto.request.UpdateTourRequest;
import clyvasync.Clyvasync.dto.response.TourDetailResponse;
import clyvasync.Clyvasync.dto.response.TourResponse;
import clyvasync.Clyvasync.enums.type.TourStatus;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
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
import org.jspecify.annotations.Nullable;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
@AllArgsConstructor
public class TourServiceImpl implements TourService {
    private final TourRepository tourRepository;
    private final TourImageService tourImageService;
    private final TourMapper tourMapper;
    private final clyvasync.Clyvasync.repository.homestay.HomestayRepository homestayRepository;
    private final clyvasync.Clyvasync.repository.tour.TourImageRepository tourImageRepository;
    private final clyvasync.Clyvasync.utils.MediaUtil mediaUtil;

    @Override
    @Transactional
    public TourResponse createTour(Long currentOwnerId, Long homestayId, CreateTourRequest request) {
        clyvasync.Clyvasync.modules.homestay.entity.Homestay homestay = homestayRepository.findById(homestayId)
                .orElseThrow(() -> new AppException(ResultCode.HOMESTAY_NOT_FOUND));

        if (!homestay.getOwnerId().equals(currentOwnerId)) {
            throw new AppException(ResultCode.PERMISSION_DENIED);
        }

        Long ownerId = homestay.getOwnerId();

        Tour tour = new Tour();
        tour.setHomestayId(homestayId);
        tour.setName(request.name());
        tour.setDescription(request.description());
        tour.setDurationType(request.durationType().name());
        tour.setDurationValue(request.durationValue());
        tour.setPricePerPerson(request.pricePerPerson());
        tour.setMaxParticipants(request.maxParticipants());
        tour.setAllowExternalGuests(request.allowExternalGuests() != null ? request.allowExternalGuests() : false);
        tour.setStatus(TourStatus.ACTIVE);
        
        tour = tourRepository.save(tour);

        List<String> imageKeys = request.imageKeys();
        if (imageKeys != null && !imageKeys.isEmpty()) {
            List<clyvasync.Clyvasync.modules.tour.entity.TourImage> pendingImages = tourImageRepository.findByOwnerIdAndStatusAndImageUrlIn(
                    ownerId, clyvasync.Clyvasync.enums.media.MediaStatus.PENDING, imageKeys);
            
            for (clyvasync.Clyvasync.modules.tour.entity.TourImage image : pendingImages) {
                image.setTourId(tour.getId());
                image.setStatus(clyvasync.Clyvasync.enums.media.MediaStatus.ACTIVE);
            }
            tourImageRepository.saveAll(pendingImages);
        }

        return tourMapper.toResponse(tour, null, null);
    }

    @Override
    @Transactional
    public TourResponse updateTour(Long tourId, UpdateTourRequest request) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new AppException(ResultCode.TOUR_NOT_FOUND));

        clyvasync.Clyvasync.modules.homestay.entity.Homestay homestay = homestayRepository.findById(tour.getHomestayId())
                .orElseThrow(() -> new AppException(ResultCode.HOMESTAY_NOT_FOUND));
        
        Long ownerId = homestay.getOwnerId();

        tour.setName(request.name());
        tour.setDescription(request.description());
        if (request.durationType() != null) {
            tour.setDurationType(request.durationType().name());
        }
        if (request.durationValue() != null) {
            tour.setDurationValue(request.durationValue());
        }
        if (request.pricePerPerson() != null) {
            tour.setPricePerPerson(request.pricePerPerson());
        }
        if (request.maxParticipants() != null) {
            tour.setMaxParticipants(request.maxParticipants());
        }
        if (request.allowExternalGuests() != null) {
            tour.setAllowExternalGuests(request.allowExternalGuests());
        }

        tour = tourRepository.save(tour);

        List<String> imageKeys = request.imageKeys();
        if (imageKeys != null && !imageKeys.isEmpty()) {
            // Có thể xóa ảnh cũ hoặc đánh dấu INACTIVE ở đây nếu muốn. Hiện tại ta update/thêm ảnh mới
            List<clyvasync.Clyvasync.modules.tour.entity.TourImage> pendingImages = tourImageRepository.findByOwnerIdAndStatusAndImageUrlIn(
                    ownerId, clyvasync.Clyvasync.enums.media.MediaStatus.PENDING, imageKeys);
            
            for (clyvasync.Clyvasync.modules.tour.entity.TourImage image : pendingImages) {
                image.setTourId(tour.getId());
                image.setStatus(clyvasync.Clyvasync.enums.media.MediaStatus.ACTIVE);
            }
            tourImageRepository.saveAll(pendingImages);
        }

        // Fetch new images
        List<clyvasync.Clyvasync.modules.tour.entity.TourImage> allImages = tourImageRepository.findByTourIdIn(List.of(tour.getId()));
        String primary = null;
        String hover = null;
        for (clyvasync.Clyvasync.modules.tour.entity.TourImage img : allImages) {
            if (img.getStatus() == clyvasync.Clyvasync.enums.media.MediaStatus.ACTIVE) {
                if (primary == null) primary = img.getImageUrl();
                else if (hover == null) hover = img.getImageUrl();
            }
        }

        String primaryUrl = primary != null ? mediaUtil.toCdnUrl(primary) : null;
        String hoverUrl = hover != null ? mediaUtil.toCdnUrl(hover) : null;

        return tourMapper.toResponse(tour, primaryUrl, hoverUrl);
    }

    @Override
    @Transactional
    public void deleteTour(Long tourId) {
        Tour tour = tourRepository.findById(tourId)
                .orElseThrow(() -> new AppException(ResultCode.TOUR_NOT_FOUND));
        tour.setStatus(TourStatus.INACTIVE);
        tourRepository.save(tour);
    }

    @Override
    public void updateTourStatus(Long tourId, TourStatus status) {

    }

    @Override
    public TourDetailResponse getTourById(Long tourId) {
        return null;
    }

    @Override

    public List<TourResponse> getToursByHomestayId(Long homestayId) {
        log.info("Lấy danh sách tour cho homestay: {}", homestayId);

        List<Tour> tours = tourRepository.findAllByHomestayIdAndStatus(homestayId, TourStatus.ACTIVE);
        if (tours.isEmpty()) return List.of();

        List<Long> tourIds = tours.stream().map(Tour::getId).toList();

        Map<Long, List<String>> imagesMap = tourImageService.getImagesForTours(tourIds);

        return tours.stream().map(entity -> {
            List<String> urls = imagesMap.getOrDefault(entity.getId(), List.of());

            String primary = !urls.isEmpty() ? urls.get(0) : null;
            String hover = urls.size() > 1 ? urls.get(1) : primary;

            String primaryUrl = primary != null ? mediaUtil.toCdnUrl(primary) : null;
            String hoverUrl = hover != null ? mediaUtil.toCdnUrl(hover) : null;

            return tourMapper.toResponse(entity, primaryUrl, hoverUrl);
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
    @Transactional(readOnly = true)
    public Page<TourResponse> getAllTours(Pageable pageable) {
        Page<Tour> tourPage = tourRepository.findAll(pageable);

        if (tourPage.isEmpty()) {
            return Page.empty();
        }

        List<Long> tourIds = tourPage.getContent().stream()
                .map(Tour::getId)
                .toList();

        Map<Long, List<String>> imagesMap = tourImageService.getImagesForTours(tourIds);

        return tourPage.map(entity -> {
            List<String> urls = imagesMap.getOrDefault(entity.getId(), List.of());

            String primary = !urls.isEmpty() ? urls.get(0) : null;
            String hover = urls.size() > 1 ? urls.get(1) : primary;

            String primaryUrl = primary != null ? mediaUtil.toCdnUrl(primary) : null;
            String hoverUrl = hover != null ? mediaUtil.toCdnUrl(hover) : null;

            return tourMapper.toResponse(entity, primaryUrl, hoverUrl);
        });
    }

    @Override
    public List<TourResponse> getAvailableToursForBookingDates(Long homestayId, LocalDate checkIn, LocalDate checkOut) {
        List<Tour> availableTours = tourRepository.findAvailableToursByDates(homestayId, checkIn, checkOut);
        log.info(availableTours.toString());

        if (availableTours.isEmpty()) {
            return List.of();
        }
        List<Long> tourIds = availableTours.stream().map(Tour::getId).toList();

        Map<Long, List<String>> imagesMap = tourImageService.getImagesForTours(tourIds);

        return availableTours.stream().map(entity -> {
            List<String> urls = imagesMap.getOrDefault(entity.getId(), List.of());

            String primary = !urls.isEmpty() ? urls.get(0) : null;
            String hover = urls.size() > 1 ? urls.get(1) : primary;

            String primaryUrl = primary != null ? mediaUtil.toCdnUrl(primary) : null;
            String hoverUrl = hover != null ? mediaUtil.toCdnUrl(hover) : null;

            return tourMapper.toResponse(entity, primaryUrl, hoverUrl);
        }).toList();
    }

    @Override
    public Tour findTourById(Long tourId) {
        return tourRepository.findById(tourId).orElseThrow(() -> new AppException(ResultCode.TOUR_NOT_FOUND));
    }

    @Override
    public List<Tour> findAllByIds(List<Long> tourIds) {
        return tourRepository.findAllById(tourIds);
    }

    @Override
    public List<Tour> findByIdIn(List<Long> ids) {
        return tourRepository.findByIdIn(ids);
    }

    @Override
    public Page<Tour> findAll(@Nullable Specification<Tour> spec, Pageable pageable) {
        return tourRepository.findAll(spec,pageable);
    }
}

