package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.dto.request.HomestayRequest;
import clyvasync.Clyvasync.dto.request.HomestaySearchRequest;
import clyvasync.Clyvasync.dto.response.*;
import clyvasync.Clyvasync.enums.homestay.HomestayStatus;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.mapper.homestay.AmenityMapper;
import clyvasync.Clyvasync.mapper.homestay.HomestayMapper;
import clyvasync.Clyvasync.modules.auth.entity.User;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayImage;
import clyvasync.Clyvasync.modules.homestay.entity.Location;
import clyvasync.Clyvasync.repository.homestay.AmenityRepository;
import clyvasync.Clyvasync.repository.homestay.HomestayImageRepository;
import clyvasync.Clyvasync.repository.homestay.HomestayRepository;
import clyvasync.Clyvasync.service.auth.UserService;
import clyvasync.Clyvasync.service.booking.BookingService;
import clyvasync.Clyvasync.service.homestay.*;
import clyvasync.Clyvasync.service.tour.TourService;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class HomestayServiceImpl implements HomestayService {
    private final HomestayRepository homestayRepository;
    private final HomestayMapper homestayMapper;
    private final AmenityService amenityService;
    private final HomestayImageService homestayImageService;
    private final LocationService locationService;
    private final CategoryService categoryService;
    private final ReviewService reviewService;
    private final TourService tourService;
    private final UserService userService;
    private final HomestayRoomService homestayRoomService;
    private final BookingService bookingService;

    @Override
    public HomestayResponse createHomestay(HomestayRequest request, Long ownerId) {
        return null;
    }

    @Override
    public HomestayResponse updateHomestay(Long id, HomestayRequest request, Long ownerId) {
        return null;
    }

    @Override
    public void deleteHomestay(Long id, Long ownerId) {

    }

    @Override
    public HomestayResponse getById(Long id) {
        return null;
    }

    @Override
    public Page<HomestayResponse> searchHomestays(HomestaySearchRequest filters, Pageable pageable) {
        log.info("Searching homestays with filters: {}", filters);

        Specification<Homestay> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. Lọc theo Thành phố (Dùng Subquery vì là ID phẳng)
            if (StringUtils.hasText(filters.city())) {
                Subquery<Integer> locationSubquery = query.subquery(Integer.class);
                Root<Location> locationRoot = locationSubquery.from(Location.class);
                locationSubquery.select(locationRoot.get("id"));

                Predicate cityMatch = cb.or(
                        cb.like(cb.lower(locationRoot.get("cityName")), "%" + filters.city().toLowerCase() + "%"),
                        cb.equal(locationRoot.get("slug"), filters.city())
                );
                locationSubquery.where(cityMatch);

                predicates.add(cb.in(root.get("locationId")).value(locationSubquery));
            }

            // 2. Lọc theo khoảng giá
            if (filters.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("basePrice"), filters.minPrice()));
            }
            if (filters.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("basePrice"), filters.maxPrice()));
            }

            // 3. Lọc theo số khách
            if (filters.guests() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("maxGuests"), filters.guests()));
            }

            // 4. Lọc theo Rating (Check null trong Entity trước khi dùng)
            if (filters.minRating() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("averageRating"), filters.minRating()));
            }

            // 5. Lọc theo CategoryId (ID phẳng)
            if (filters.categoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), filters.categoryId()));
            }

            // 6. Luôn lọc bỏ những căn đã bị xóa mềm
            predicates.add(cb.isNull(root.get("deletedAt")));

            // 7. FIX LỖI ENUM: So sánh Object Enum với Object Enum
            predicates.add(cb.equal(root.get("status"), HomestayStatus.AVAILABLE));

            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Homestay> homestayPage = homestayRepository.findAll(spec, pageable);
        List<Homestay> homestays = homestayPage.getContent();
        List<Long> ids = homestays.stream().map(Homestay::getId).toList();
        List<Integer> locationIds = homestays.stream().map(Homestay::getLocationId).distinct().toList();
        List<Integer> categoryIds = homestays.stream().map(Homestay::getCategoryId).distinct().toList();

        Map<Long, List<AmenityResponse>> amenitiesMap = amenityService.getAmenitiesForHomestays(ids);
        Map<Long, List<String>> imagesMap = homestayImageService.getImagesForHomestays(ids);
        Map<Integer, String> locationsMap = locationService.getLocationNamesMap(locationIds);
        Map<Integer, String> categoriesMap = categoryService.getCategoryNamesMap(categoryIds);

        return homestayPage.map(entity -> {
            HomestayResponse response = homestayMapper.toResponse(entity);
            response.setImageUrls(imagesMap.getOrDefault(entity.getId(), List.of()));
            response.setCityName(locationsMap.get(entity.getLocationId()));
            response.setCategoryName(categoriesMap.get(entity.getCategoryId()));
            response.setAmenities(amenitiesMap.getOrDefault(entity.getId(), List.of()));
            response.setAverageRating(BigDecimal.valueOf(entity.getAverageRating() != null ? entity.getAverageRating().doubleValue() : 0.0));

            return response;
        });
    }


    @Override
    public List<HomestayResponse> getByOwnerId(Long ownerId) {
        return List.of();
    }

    @Override
    public void updateStatus(Long id, String status, Long ownerId) {

    }

    @Override
    public void updateAverageRating(Long id, BigDecimal newRating) {

    }

    @Override
    public HomestayDetailResponse getHomestayDetail(Long currentUserId, Long id) {
        log.info("Getting homestay detail for user {} with ID {}", currentUserId, id);

        Homestay homestay = homestayRepository.findById(id)
                .orElseThrow(() -> new AppException(ResultCode.HOMESTAY_NOT_FOUND));

        List<String> images = homestayImageService.getImagesByHomestayId(id);

        List<AmenityResponse> amenities = amenityService.getAmenitiesByHomestayId(id);

        String cityName = locationService.getLocationNamesMap(List.of(homestay.getLocationId()))
                .get(homestay.getLocationId());

        String categoryName = categoryService.getCategoryNamesMap(List.of(homestay.getCategoryId()))
                .get(homestay.getCategoryId());
        List<ReviewResponse> reviews = reviewService.getReviewsByHomestayId(id);
        List<RoomResponse> rooms = homestayRoomService.getAllRoomsByHomestay(id);


        return HomestayDetailResponse.builder()
                .id(homestay.getId())
                .name(homestay.getName())
                .description(homestay.getDescription())
                .addressDetail(homestay.getAddressDetail())
                .basePrice(homestay.getBasePrice())
                .maxGuests(homestay.getMaxGuests())
                .numBedrooms(homestay.getNumBedrooms())
                .numBathrooms(homestay.getNumBathrooms())
                .latitude(homestay.getLatitude() != null ? homestay.getLatitude().doubleValue() : null)
                .longitude(homestay.getLongitude() != null ? homestay.getLongitude().doubleValue() : null)
                .status(homestay.getStatus())
                .averageRating(homestay.getAverageRating())
                .reviewCount(homestay.getReviewCount())
                .cityName(cityName)
                .categoryName(categoryName)
                .imageUrls(images)
                .amenities(amenities)
                .owner(userService.getOwnerInfo(homestay.getOwnerId()))
                .reviews(reviews)
                .tours(tourService.getToursByHomestayId(homestay.getId()))
                .rooms(rooms)
                .build();
    }
}