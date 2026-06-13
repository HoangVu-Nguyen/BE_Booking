package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.dto.detail.PropertyStats;
import clyvasync.Clyvasync.dto.projection.BookingTimelineProjection;
import clyvasync.Clyvasync.dto.request.GlobalSearchRequest;
import clyvasync.Clyvasync.dto.request.HomestayRequest;
import clyvasync.Clyvasync.dto.request.HomestaySearchRequest;
import clyvasync.Clyvasync.dto.response.*;
import clyvasync.Clyvasync.dto.summary.HomestayRoomSummary;
import clyvasync.Clyvasync.enums.booking.BookingStatus;
import clyvasync.Clyvasync.enums.homestay.HomestayStatus;
import clyvasync.Clyvasync.enums.media.MediaStatus;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.mapper.homestay.AmenityMapper;
import clyvasync.Clyvasync.mapper.homestay.HomestayMapper;
import clyvasync.Clyvasync.modules.auth.entity.User;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayImage;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayRoom;
import clyvasync.Clyvasync.modules.homestay.entity.Location;
import clyvasync.Clyvasync.modules.room.RoomCalendar;
import clyvasync.Clyvasync.modules.tour.entity.Tour;
import clyvasync.Clyvasync.modules.tour.entity.TourImage;
import clyvasync.Clyvasync.repository.homestay.AmenityRepository;
import clyvasync.Clyvasync.repository.homestay.HomestayImageRepository;
import clyvasync.Clyvasync.repository.homestay.HomestayRepository;
import clyvasync.Clyvasync.repository.tour.TourRepository;
import clyvasync.Clyvasync.service.annotation.IsHomestayOwner;
import clyvasync.Clyvasync.service.auth.UserService;
import clyvasync.Clyvasync.service.booking.BookingDetailService;
import clyvasync.Clyvasync.service.booking.BookingService;
import clyvasync.Clyvasync.service.homestay.*;
import clyvasync.Clyvasync.service.room.RoomCalendarService;
import clyvasync.Clyvasync.service.tour.TourImageService;
import clyvasync.Clyvasync.service.tour.TourService;
import clyvasync.Clyvasync.spec.HomestaySearchSpec;
import clyvasync.Clyvasync.spec.TourSearchSpec;
import clyvasync.Clyvasync.utils.MediaUtil;
import jakarta.persistence.criteria.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
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
    private final FavoriteService favoriteService;
    private final TourImageService tourImageService;
    private final RoomCalendarService roomCalendarService;
    private final BookingDetailService bookingDetailService;
    private final BookingService bookingService;
    private final MediaUtil mediaUtil;

    public HomestayServiceImpl(HomestayRepository homestayRepository, HomestayMapper homestayMapper, AmenityService amenityService, HomestayImageService homestayImageService, LocationService locationService, CategoryService categoryService, ReviewService reviewService, TourService tourService, UserService userService, HomestayRoomService homestayRoomService, FavoriteService favoriteService, TourImageService tourImageService, RoomCalendarService roomCalendarService, BookingDetailService bookingDetailService,@Lazy BookingService bookingService,MediaUtil mediaUtil) {
        this.homestayRepository = homestayRepository;
        this.homestayMapper = homestayMapper;
        this.amenityService = amenityService;
        this.homestayImageService = homestayImageService;
        this.locationService = locationService;
        this.categoryService = categoryService;
        this.reviewService = reviewService;
        this.tourService = tourService;
        this.userService = userService;
        this.homestayRoomService = homestayRoomService;
        this.favoriteService = favoriteService;
        this.tourImageService = tourImageService;
        this.roomCalendarService = roomCalendarService;
        this.bookingDetailService = bookingDetailService;
        this.bookingService = bookingService;
        this.mediaUtil = mediaUtil;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public HomestayResponse createHomestay(HomestayRequest request, Long ownerId) {
        Homestay homestay = homestayMapper.toEntity(request);
        homestay.setOwnerId(ownerId);
        homestay.setStatus(HomestayStatus.DRAFT);
        if (request.getCity() != null && !request.getCity().trim().isEmpty()) {
            Optional<Integer> locationIdOpt = locationService.findIdByNameOrSlug(request.getCity().trim());

            locationIdOpt.ifPresent(homestay::setLocationId);
        }
        Homestay savedHomestay = homestayRepository.save(homestay);

        // 2. MAPPING ẢNH (Từ PENDING sang ACTIVE)
        if (request.getObjectKeys() != null && !request.getObjectKeys().isEmpty()) {
            List<HomestayImage> imagesToMap = homestayImageService.findByImageUrlIn(request.getObjectKeys());

            for (HomestayImage img : imagesToMap) {
                img.setHomestayId(savedHomestay.getId());
                img.setStatus(MediaStatus.ACTIVE);
            }

            homestayImageService.saveAll(imagesToMap);
        }

        return homestayMapper.toResponse(savedHomestay);
    }

    @Override
    @IsHomestayOwner
    public HomestayResponse updateHomestay(Long id, HomestayRequest request, Long ownerId) {
    Homestay homestay = homestayRepository.findById(id)
            .orElseThrow(() -> new AppException(ResultCode.HOMESTAY_NOT_FOUND));

    if (StringUtils.hasText(request.getName())) {
        homestay.setName(request.getName());
    }
    if (StringUtils.hasText(request.getDescription())) {
        homestay.setDescription(request.getDescription());
    }
    if (StringUtils.hasText(request.getAddressDetail())) {
        homestay.setAddressDetail(request.getAddressDetail());
    }
    if (request.getLatitude() != null) {
        homestay.setLatitude(request.getLatitude());
    }
    if (request.getLongitude() != null) {
        homestay.setLongitude(request.getLongitude());
    }
    if (request.getCategoryId() != null) {
        homestay.setCategoryId(request.getCategoryId());
    }

    if (StringUtils.hasText(request.getCity())) {
        Optional<Integer> locationIdOpt = locationService.findIdByNameOrSlug(request.getCity().trim());
        locationIdOpt.ifPresent(homestay::setLocationId);
    }

    Homestay updatedHomestay = homestayRepository.save(homestay);

    if (request.getObjectKeys() != null && !request.getObjectKeys().isEmpty()) {
        List<HomestayImage> imagesToMap = homestayImageService.findByImageUrlIn(request.getObjectKeys());
        for (HomestayImage img : imagesToMap) {
            img.setHomestayId(updatedHomestay.getId());
            img.setStatus(MediaStatus.ACTIVE);
        }
        homestayImageService.saveAll(imagesToMap);
    }

    HomestayResponse response = homestayMapper.toResponse(updatedHomestay);

    response.setImageUrls(mediaUtil.toCdnUrls(homestayImageService.getImagesByHomestayId(id)));
    response.setAmenities(amenityService.getAmenitiesByHomestayId(id));

    Map<Integer, String> locationsMap = locationService.getLocationNamesMap(List.of(updatedHomestay.getLocationId()));
    response.setCityName(locationsMap.get(updatedHomestay.getLocationId()));

    Map<Integer, String> categoriesMap = categoryService.getCategoryNamesMap(List.of(updatedHomestay.getCategoryId()));
    response.setCategoryName(categoriesMap.get(updatedHomestay.getCategoryId()));

    response.setOwner(userService.getOwnerInfo(updatedHomestay.getOwnerId()));

    List<HomestayRoomSummary> summaries = homestayRoomService.getRoomSummaries(List.of(id));
    if (summaries != null && !summaries.isEmpty()) {
        HomestayRoomSummary summary = summaries.get(0);
        response.setBasePrice(summary.getMinPrice());
        response.setMaxGuests(summary.getMaxGuestsInRoom());
        response.setNumBedrooms(summary.getTotalRooms());
        response.setNumBathrooms(summary.getTotalRooms());
    } else {
        response.setBasePrice(BigDecimal.ZERO);
        response.setMaxGuests(0);
        response.setNumBedrooms(0);
        response.setNumBathrooms(0);
    }

    response.setAverageRating(BigDecimal.valueOf(updatedHomestay.getAverageRating() != null ? updatedHomestay.getAverageRating().doubleValue() : 0.0));

    return response;
    }
    @Override
    @IsHomestayOwner
    public void deleteHomestay(Long id, Long ownerId) {

    }

    @Override
    public HomestayResponse getById(Long id) {
        // 1. Lấy thực thể Homestay gốc
        Homestay homestay = homestayRepository.findById(id)
                .orElseThrow(() -> new AppException(ResultCode.HOMESTAY_NOT_FOUND));

        // 2. Map các trường cơ bản từ Entity sang Response
        HomestayResponse response = homestayMapper.toResponse(homestay);

        // 3. Lắp ráp dữ liệu Hình ảnh và Tiện ích
        response.setImageUrls(mediaUtil.toCdnUrls(homestayImageService.getImagesByHomestayId(id))); // Cần chắc chắn hàm này có tồn tại trong Service
        response.setAmenities(amenityService.getAmenitiesByHomestayId(id));

        // 4. Lắp ráp tên Thành phố và Danh mục
        Map<Integer, String> locationsMap = locationService.getLocationNamesMap(List.of(homestay.getLocationId()));
        response.setCityName(locationsMap.get(homestay.getLocationId()));

        Map<Integer, String> categoriesMap = categoryService.getCategoryNamesMap(List.of(homestay.getCategoryId()));
        response.setCategoryName(categoriesMap.get(homestay.getCategoryId()));

        // 5. Lắp ráp thông tin Chủ nhà (Owner)
        response.setOwner(userService.getOwnerInfo(homestay.getOwnerId()));

        // 6. Lắp ráp thông tin quy mô Phòng (Giá thấp nhất, Số khách, Số phòng)
        List<HomestayRoomSummary> summaries = homestayRoomService.getRoomSummaries(List.of(id));
        if (summaries != null && !summaries.isEmpty()) {
            HomestayRoomSummary summary = summaries.get(0);
            response.setBasePrice(summary.getMinPrice());
            response.setMaxGuests(summary.getMaxGuestsInRoom());
            response.setNumBedrooms(summary.getTotalRooms());
            response.setNumBathrooms(summary.getTotalRooms()); // Tạm dùng chung theo logic cũ của bạn
        } else {
            response.setBasePrice(BigDecimal.ZERO);
            response.setMaxGuests(0);
            response.setNumBedrooms(0);
            response.setNumBathrooms(0);
        }

        // 7. Xử lý Rating (Đảm bảo không bị null)
        response.setAverageRating(BigDecimal.valueOf(homestay.getAverageRating() != null ? homestay.getAverageRating().doubleValue() : 0.0));

        return response;
    }

    @Override
    public Page<HomestayResponse> searchHomestays(HomestaySearchRequest filters, Pageable pageable) {
        log.info("[SEARCH V2] Searching homestays with cinematic filters: {}", filters);

        Specification<Homestay> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 1. TÌM KIẾM TỪ KHÓA (Gần đúng, không phân biệt hoa thường)
            if (StringUtils.hasText(filters.city())) {
                // Mẹo Postgres: Nếu DB bác có cài EXTENSION unaccent, bác có thể đổi cb.lower thành:
                // cb.lower(cb.function("unaccent", String.class, ...)) để tìm tiếng Việt không dấu siêu chuẩn.
                String searchPattern = "%" + filters.city().trim().toLowerCase() + "%";

                // 1.1 Tìm trong tên Thành phố (Location Subquery)
                Subquery<Integer> locationSubquery = query.subquery(Integer.class);
                Root<Location> locationRoot = locationSubquery.from(Location.class);
                locationSubquery.select(locationRoot.get("id"));
                Predicate cityMatch = cb.or(
                        cb.like(cb.lower(locationRoot.get("cityName")), searchPattern),
                        cb.equal(locationRoot.get("slug"), filters.city())
                );
                locationSubquery.where(cityMatch);
                Predicate matchLocation = root.get("locationId").in(locationSubquery);

                // 1.2 Tìm trực tiếp trong tên Homestay
                Predicate matchName = cb.like(
                        cb.lower(cb.function("unaccent", String.class, root.get("name"))),
                        cb.function("unaccent", String.class, cb.literal(searchPattern))
                );

                // Gộp lại: Có trong Tên HOẶC Có trong Thành phố đều lấy
                predicates.add(cb.or(matchName, matchLocation));
            }

            // 2. LỌC THEO GIÁ (Ngân sách)
            if (filters.minPrice() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("basePrice"), filters.minPrice()));
            }
            if (filters.maxPrice() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("basePrice"), filters.maxPrice()));
            }

            // 3. QUY MÔ (Số khách & Phòng ngủ) - Chỉ lọc nếu > 0
            if (filters.guests() != null && filters.guests() > 0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("maxGuests"), filters.guests()));
            }
            if (filters.bedrooms() != null && filters.bedrooms() > 0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("numBedrooms"), filters.bedrooms()));
            }

            // 4. LỌC THEO SỐ SAO (Rating)
            if (filters.minRating() != null && filters.minRating() > 0) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("averageRating"), filters.minRating()));
            }

            // 5. LỌC CATEGORY (Tour hay Homestay)
            if (filters.categoryId() != null) {
                predicates.add(cb.equal(root.get("categoryId"), filters.categoryId()));
            }

            // 6. LỌC TIỆN ÍCH (Yêu cầu phải có TẤT CẢ các tiện ích được chọn)
            if (filters.amenityIds() != null && !filters.amenityIds().isEmpty()) {
                // Tùy vào cách cấu hình Entity của bác:
                // CÁCH 1 (Dễ nhất): Nếu trong entity Homestay bác có @ManyToMany List<Amenity> amenities
            /*
            for (Integer amId : filters.amenityIds()) {
                predicates.add(cb.isMember(amId, root.get("amenities"))); // Hibernate tự JOIN bảng phụ
            }
            */

                // CÁCH 2: Nếu bác lưu Array List Integer thẳng vào PostgreSQL (Cột JSONB hoặc INT[])
            for (Integer amId : filters.amenityIds()) {
                predicates.add(cb.isTrue(cb.function("jsonb_contains", Boolean.class, root.get("amenityIds"), cb.literal(amId.toString()))));
            }


                // Note: Bác mở comment cách nào phù hợp với kiến trúc Entity của bác nhé!
            }

            // 7. FIX CỨNG: Trạng thái hiển thị
            predicates.add(cb.isNull(root.get("deletedAt")));
            predicates.add(cb.equal(root.get("status"), HomestayStatus.AVAILABLE));

            // Nếu query.where() chưa được gọi, JPA tự hiểu là lấy danh sách các Predicate này nối với nhau bằng AND
            return cb.and(predicates.toArray(new Predicate[0]));
        };

        // THỰC THI QUERY ĐỘNG TỚI DB
        Page<Homestay> homestayPage = homestayRepository.findAll(spec, pageable);
        List<Homestay> homestays = homestayPage.getContent();

        if (homestays.isEmpty()) {
            return Page.empty(pageable); // Thoát sớm nếu không tìm thấy, tránh chạy code thừa
        }

        // =========================================================================
        // ĐOẠN DƯỚI NÀY LÀ TUYỆT KỸ BÁC VŨ ĐÃ VIẾT ĐỂ CHỐNG N+1 QUERY (Giữ nguyên)
        // =========================================================================
        List<Long> ids = homestays.stream().map(Homestay::getId).toList();
        List<Integer> locationIds = homestays.stream().map(Homestay::getLocationId).distinct().toList();
        List<Integer> categoryIds = homestays.stream().map(Homestay::getCategoryId).distinct().toList();

        List<HomestayRoomSummary> summaries = homestayRoomService.getRoomSummaries(ids);
        Map<Long, HomestayRoomSummary> roomSummaryMap = summaries.stream()
                .collect(Collectors.toMap(HomestayRoomSummary::getHomestayId, s -> s));

        Map<Long, List<AmenityResponse>> amenitiesMap = amenityService.getAmenitiesForHomestays(ids);
        Map<Long, List<String>> imagesMap = homestayImageService.getImagesForHomestays(ids);
        Map<Integer, String> locationsMap = locationService.getLocationNamesMap(locationIds);
        Map<Integer, String> categoriesMap = categoryService.getCategoryNamesMap(categoryIds);

        // MAPPING SANG DTO
        return homestayPage.map(entity -> {
            HomestayResponse response = homestayMapper.toResponse(entity);
            response.setImageUrls(mediaUtil.toCdnUrls(imagesMap.getOrDefault(entity.getId(), List.of())));
            response.setCityName(locationsMap.get(entity.getLocationId()));
            response.setCategoryName(categoriesMap.get(entity.getCategoryId()));
            response.setAmenities(amenitiesMap.getOrDefault(entity.getId(), List.of()));

            response.setAverageRating(BigDecimal.valueOf(entity.getAverageRating() != null ? entity.getAverageRating().doubleValue() : 0.0));

            HomestayRoomSummary summary = roomSummaryMap.get(entity.getId());
            if (summary != null) {
                response.setBasePrice(summary.getMinPrice());
                response.setMaxGuests(summary.getMaxGuestsInRoom());
                response.setNumBedrooms(summary.getTotalRooms());
                response.setNumBathrooms(summary.getTotalRooms()); // Tạm dùng chung theo logic cũ của bác
            } else {
                response.setBasePrice(BigDecimal.ZERO);
                response.setMaxGuests(0);
                response.setNumBedrooms(0);
            }
            return response;
        });
    }

    @Override
    public List<HomestayResponse> getByOwnerId(Long ownerId) {
        // 1. Lấy danh sách Homestay của đúng Host đó
        List<Homestay> homestays = homestayRepository.findAllByOwnerId(ownerId);
        List<Long> ids = homestays.stream().map(Homestay::getId).toList();
        List<Integer> locationIds = homestays.stream().map(Homestay::getLocationId).distinct().toList();
        List<Integer> categoryIds = homestays.stream().map(Homestay::getCategoryId).distinct().toList();

        List<HomestayRoomSummary> summaries = homestayRoomService.getRoomSummaries(ids);
        Map<Long, HomestayRoomSummary> roomSummaryMap = summaries.stream()
                .collect(Collectors.toMap(HomestayRoomSummary::getHomestayId, s -> s));

        Map<Long, List<AmenityResponse>> amenitiesMap = amenityService.getAmenitiesForHomestays(ids);
        Map<Long, List<String>> imagesMap = homestayImageService.getImagesForHomestays(ids);
        Map<Integer, String> locationsMap = locationService.getLocationNamesMap(locationIds);
        Map<Integer, String> categoriesMap = categoryService.getCategoryNamesMap(categoryIds);

        // MAPPING SANG DTO
        return homestays.stream().map(entity -> {
            HomestayResponse response = homestayMapper.toResponse(entity);
            response.setImageUrls(mediaUtil.toCdnUrls(imagesMap.getOrDefault(entity.getId(), List.of())));
            response.setCityName(locationsMap.get(entity.getLocationId()));
            response.setCategoryName(categoriesMap.get(entity.getCategoryId()));
            response.setAmenities(amenitiesMap.getOrDefault(entity.getId(), List.of()));

            response.setAverageRating(BigDecimal.valueOf(entity.getAverageRating() != null ? entity.getAverageRating().doubleValue() : 0.0));

            HomestayRoomSummary summary = roomSummaryMap.get(entity.getId());
            if (summary != null) {
                response.setBasePrice(summary.getMinPrice());
                response.setMaxGuests(summary.getMaxGuestsInRoom());
                response.setNumBedrooms(summary.getTotalRooms());
                response.setNumBathrooms(summary.getTotalRooms());
            } else {
                response.setBasePrice(BigDecimal.ZERO);
                response.setMaxGuests(0);
                response.setNumBedrooms(0);
            }
            return response;
        }).toList();


    }

    @Override
    public List<Homestay> findByOwnerId(Long ownerId) {
        return homestayRepository.findAllByOwnerId(ownerId);
    }

    @Override
    @IsHomestayOwner
    public void updateStatus(Long id, String status, Long ownerId) {

    }

    @Override
    @IsHomestayOwner
    public void updateAverageRating(Long id, BigDecimal newRating) {

    }

    @Override
    public HomestayDetailResponse getHomestayDetail(Long currentUserId, Long id, LocalDate checkIn, LocalDate checkOut, Integer guests) {
        log.info("Getting homestay detail for user {} with ID {}", currentUserId, id);

        Homestay homestay = homestayRepository.findById(id)
                .orElseThrow(() -> new AppException(ResultCode.HOMESTAY_NOT_FOUND));

        List<String> images = mediaUtil.toCdnUrls(
                homestayImageService.getImagesByHomestayId(id)
        );

        List<AmenityResponse> amenities = amenityService.getAmenitiesByHomestayId(id);

        String cityName = locationService.getLocationNamesMap(List.of(homestay.getLocationId()))
                .get(homestay.getLocationId());

        String categoryName = categoryService.getCategoryNamesMap(List.of(homestay.getCategoryId()))
                .get(homestay.getCategoryId());
        List<ReviewResponse> reviews = reviewService.getReviewsByHomestayId(id);
        List<RoomResponse> rooms;
        if (checkIn != null && checkOut != null) {
            // Nếu khách đã chọn ngày, chỉ hiện phòng còn trống
            int guestCount = (guests != null) ? guests : 1;
            rooms = homestayRoomService.findAvailableRooms(id, checkIn, checkOut, guestCount);
        } else {
            // Nếu khách vào xem chung, hiện tất cả phòng để họ tham khảo
            rooms = homestayRoomService.getAllRoomsByHomestay(id);
        }


        return HomestayDetailResponse.builder()
                .id(homestay.getId())
                .name(homestay.getName())
                .description(homestay.getDescription())
                .addressDetail(homestay.getAddressDetail())
                .latitude(homestay.getLatitude() != null ? homestay.getLatitude().doubleValue() : null)
                .longitude(homestay.getLongitude() != null ? homestay.getLongitude().doubleValue() : null)
                .status(homestay.getStatus())
                .averageRating(homestay.getAverageRating())
                .reviewCount(homestay.getReviewCount())
                .cityName(cityName)
                .categoryName(categoryName)
                .imageUrls(images)
                .amenities(amenities)
                .isFavorite(favoriteService.existsHomestayFavoriteByHomestayId(homestay.getId()))
                .owner(userService.getOwnerInfo(homestay.getOwnerId()))
                .reviews(reviews)
                .tours(tourService.getAvailableToursForBookingDates(homestay.getId(), checkIn, checkOut))
                .rooms(rooms)
                .build();


    }

    @Override
    public List<Homestay> findByIdIn(List<Long> ids) {
        return  homestayRepository.findByIdIn(ids);
    }

    @Override
    public List<GlobalSearchResponse> cinematicSearch(GlobalSearchRequest request) {
        log.info("[GLOBAL SEARCH] Triggered with keyword: {}", request.keyword());
        List<GlobalSearchResponse> unifiedResults = new ArrayList<>();
        String category = request.category() != null ? request.category().toUpperCase() : "ALL";

        // 1. TÌM VÀ MAP HOMESTAY
        if (category.equals("ALL") || category.equals("HOMESTAY")) {
            List<Homestay> homestays = homestayRepository.findAll(HomestaySearchSpec.buildGlobalSpec(request), PageRequest.of(0, 20)).getContent();
            System.out.println(homestays.size());
            unifiedResults.addAll(mapHomestaysToResponse(homestays));
        }

        // 2. TÌM VÀ MAP TOUR
        if (category.equals("ALL") || category.equals("TOUR")) {
            List<Tour> tours = tourService.findAll(TourSearchSpec.buildGlobalSpec(request), PageRequest.of(0, 20)).getContent();
            unifiedResults.addAll(mapToursToResponse(tours));
        }

        // 3. MIX & SORT: Ưu tiên Rating cao nhất
        unifiedResults.sort(Comparator.comparing(GlobalSearchResponse::rating, Comparator.nullsLast(Comparator.reverseOrder())));

        return unifiedResults;
    }

    @Override
    public Homestay findById(Long id) {
        return homestayRepository.findById(id).orElseThrow(() -> new AppException(ResultCode.HOMESTAY_NOT_FOUND));
    }

    @Override
    @IsHomestayOwner
    public HomestayTimelineResponse getHomestayTimeline(Long homestayId, LocalDate startDate, LocalDate endDate) {
        List<HomestayRoom> rooms = homestayRoomService.findAllById(homestayId);
        List<Long> roomIds = rooms.stream().map(HomestayRoom::getId).toList();
        if (roomIds.isEmpty()) {
            return new HomestayTimelineResponse(homestayId, startDate, endDate, List.of());
        }
        List<RoomCalendar> calendars = roomCalendarService.findCalendarsByRoomIdsAndDateRange(roomIds, startDate, endDate);
        List<BookingTimelineProjection> bookings = bookingDetailService.findOverlappingBookings(roomIds, startDate, endDate);
        Map<Long, List<RoomCalendar>> calendarMap = calendars.stream()
                .collect(Collectors.groupingBy(RoomCalendar::getRoomId));

        Map<Long, List<BookingTimelineProjection>> bookingMap = bookings.stream()
                .collect(Collectors.groupingBy(BookingTimelineProjection::roomId));
        List<RoomTimelineResponse> roomTimelines = rooms.stream().map(room -> {

            // Map Lịch giá
            List<DailyStatusResponse> dailyStatuses = calendarMap.getOrDefault(room.getId(), List.of())
                    .stream()
                    .map(c -> DailyStatusResponse.builder()
                            .date(c.getNightDate())
                            .price(c.getPriceOverride() != null ? c.getPriceOverride() : BigDecimal.ZERO) // Lắp giá gốc nếu cần
                            .availableQuantity(c.getAvailableQuantity())
                            .build())
                    .toList();

            List<BookingBlockResponse> bookingBlocks = bookingMap.getOrDefault(room.getId(), List.of())
                    .stream()
                    .map(b -> BookingBlockResponse.builder()
                            .bookingId(b.bookingId())
                            .guestName(b.guestName())
                            .checkInDate(b.checkInDate())
                            .checkOutDate(b.checkOutDate())
                            .status(b.status())
                            .build())
                    .toList();

            return RoomTimelineResponse.builder()
                    .roomId(room.getId())
                    .roomName(room.getName())
                    .dailyStatuses(dailyStatuses)
                    .bookings(bookingBlocks)
                    .build();
        }).toList();

        return new HomestayTimelineResponse(homestayId, startDate, endDate, roomTimelines);




    }

    @Override
    public Map<Long, HomestayTimelineResponse> getBatchTimeline(List<Long> homestayIds, LocalDate startDate, LocalDate endDate) {
        // 1. Lấy tất cả Rooms của tất cả Homes trong list
        List<HomestayRoom> allRooms = homestayRoomService.findAllByIdIn(homestayIds);
        List<Long> allRoomIds = allRooms.stream().map(HomestayRoom::getId).toList();

        // 2. Query 1 lần cho lịch và 1 lần cho booking (Batch)
        List<RoomCalendar> allCalendars = roomCalendarService.findCalendarsByRoomIdsAndDateRange(allRoomIds, startDate, endDate);
        List<BookingTimelineProjection> allBookings = bookingDetailService.findOverlappingBookings(allRoomIds, startDate, endDate);
        List<Long> userIds = allBookings.stream().map(BookingTimelineProjection::userId).toList();

        Map<Long,String>  ownerResponseMap = userService.getImageUsers(userIds);

        // 3. Gom nhóm dữ liệu
        Map<Long, List<RoomCalendar>> calendarMap = allCalendars.stream().collect(Collectors.groupingBy(RoomCalendar::getRoomId));
        Map<Long, List<BookingTimelineProjection>> bookingMap = allBookings.stream().collect(Collectors.groupingBy(BookingTimelineProjection::roomId));
        Map<Long, List<HomestayRoom>> roomsByHomeMap = allRooms.stream().collect(Collectors.groupingBy(HomestayRoom::getHomestayId));
        Map<Long,String> roomImageMap = homestayRoomService.getRoomImageMap(allRoomIds);
        // 4. Map sang Map<HomestayId, Timeline>
        Map<Long, HomestayTimelineResponse> result = new HashMap<>();
        for (Long homeId : homestayIds) {
            List<RoomTimelineResponse> roomTimelines = roomsByHomeMap.getOrDefault(homeId, List.of()).stream()
                    .map(room -> mapToRoomTimeline(room, calendarMap, bookingMap,roomImageMap,ownerResponseMap))
                    .toList();

            result.put(homeId, new HomestayTimelineResponse(homeId, startDate, endDate, roomTimelines));
        }
        return result;
    }


    @Override
    public PortfolioTimelineResponse getOwnerPortfolioTimeline(Long ownerId, int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.withDayOfMonth(startDate.lengthOfMonth());

        // 1. Lấy toàn bộ Homestay
        List<Homestay> allHomes = homestayRepository.findAllByOwnerId(ownerId);
        List<Long> homeIds = allHomes.stream().map(Homestay::getId).toList();

        if (homeIds.isEmpty()) return new PortfolioTimelineResponse(List.of());

        // 2. Lấy tất cả Rooms
        List<HomestayRoom> allRooms = homestayRoomService.findAllByIdIn(homeIds);
        List<Long> allRoomIds = allRooms.stream().map(HomestayRoom::getId).toList();
        Map<Long,String> roomImageMap = homestayRoomService.getRoomImageMap(allRoomIds);
        System.out.println(
                roomImageMap
        );


        // 3. Query Lịch và Booking
        List<RoomCalendar> allCalendars = roomCalendarService.findCalendarsByRoomIdsAndDateRange(allRoomIds, startDate, endDate);
        List<BookingTimelineProjection> allBookings = bookingDetailService.findOverlappingBookings(allRoomIds, startDate, endDate);

        // 4. Gom nhóm
        Map<Long, List<RoomCalendar>> calendarMap = allCalendars.stream().collect(Collectors.groupingBy(RoomCalendar::getRoomId));
        Map<Long, List<BookingTimelineProjection>> bookingMap = allBookings.stream().collect(Collectors.groupingBy(BookingTimelineProjection::roomId));
        Map<Long, List<HomestayRoom>> roomsByHomeMap = allRooms.stream().collect(Collectors.groupingBy(HomestayRoom::getHomestayId));
        List<Long> userIds = allBookings.stream().map(BookingTimelineProjection::userId).toList();
        Map<Long,String>  ownerResponseMap = userService.getImageUsers(userIds);
        System.out.println(ownerResponseMap);

        // Lấy Map ảnh (ID -> List<String>)
        Map<Long, List<String>> homestayImageMap = homestayImageService.getImagesForHomestays(homeIds);

        // 5. Map sang cấu trúc Portfolio
        List<HomeTimelineResponse> homeTimelines = allHomes.stream().map(home -> {
            List<RoomTimelineResponse> roomResponses = roomsByHomeMap.getOrDefault(home.getId(), List.of()).stream()
                    .map(room -> mapToRoomTimeline(room, calendarMap, bookingMap,roomImageMap,ownerResponseMap))
                    .toList();

            // Lấy ảnh đầu tiên làm ảnh đại diện
            List<String> images = mediaUtil.toCdnUrls(homestayImageMap.getOrDefault(home.getId(), List.of()));
            String primaryImage = !images.isEmpty() ? images.get(0) : null;

            return HomeTimelineResponse.builder()
                    .homeId(home.getId())
                    .homeName(home.getName())
                    .address(home.getAddressDetail())
                    .primaryImageUrl(primaryImage)
                    .rooms(roomResponses)
                    .build();
        }).toList();

        return new PortfolioTimelineResponse(homeTimelines);
    }

    @Override
    public List<PropertySummaryResponse> getHostProperties(Long hostId) {
        log.info("[PORTFOLIO] Fetching properties for host ID: {}", hostId);

        List<Homestay> homestays = homestayRepository.findAllByOwnerId(hostId);
        if (homestays.isEmpty()) {
            return List.of(); // Thoát sớm nếu Host chưa có tài sản nào
        }

        List<Long> homeIds = homestays.stream().map(Homestay::getId).toList();
        List<Integer> locationIds = homestays.stream().map(Homestay::getLocationId).distinct().toList();
        List<Integer> categoryIds = homestays.stream().map(Homestay::getCategoryId).distinct().toList();

        Map<Long, List<String>> imagesMap = homestayImageService.getImagesForHomestays(homeIds);
        Map<Integer, String> locationsMap = locationService.getLocationNamesMap(locationIds);
        Map<Integer, String> categoriesMap = categoryService.getCategoryNamesMap(categoryIds);

        List<HomestayRoomSummary> summaries = homestayRoomService.getRoomSummaries(homeIds);
        Map<Long, HomestayRoomSummary> roomSummaryMap = summaries.stream()
                .collect(Collectors.toMap(HomestayRoomSummary::getHomestayId, s -> s));

        List<HomestayRoom> allRooms = homestayRoomService.findAllByIdIn(homeIds);
        List<Long> allRoomIds = allRooms.stream().map(HomestayRoom::getId).toList();

        YearMonth currentMonth = YearMonth.now();
        LocalDate startOfMonth = currentMonth.atDay(1);
        LocalDate endOfMonth = currentMonth.atEndOfMonth();

        // Query lô (Batch): Lấy toàn bộ Booking của các phòng này trong tháng
        List<BookingTimelineProjection> allBookingsInMonth = new ArrayList<>();
        if (!allRoomIds.isEmpty()) { // Bảo vệ lỗi SQL IN (empty list)
            allBookingsInMonth = bookingDetailService.findOverlappingBookings(allRoomIds, startOfMonth, endOfMonth);
        }

        Map<Long, Integer> occupancyMap = calculateBatchOccupancy(homeIds, allRooms, allBookingsInMonth, startOfMonth, endOfMonth);

        return homestays.stream().map(home -> {
            HomestayRoomSummary roomSummary = roomSummaryMap.get(home.getId());
            BigDecimal basePrice = (roomSummary != null && roomSummary.getMinPrice() != null)
                    ? roomSummary.getMinPrice()
                    : BigDecimal.ZERO;

            List<String> images = imagesMap.getOrDefault(home.getId(), List.of());
            String coverImage = images.isEmpty() ? null : images.get(0);

            // Thống kê: Rating và Reviews
            Double rating = home.getAverageRating() != null ? home.getAverageRating().doubleValue() : 0.0;
            Integer reviews = home.getReviewCount() != null ? home.getReviewCount() : 0;

            Integer occupancy = occupancyMap.getOrDefault(home.getId(), 0);

            return PropertySummaryResponse.builder()
                    .id(home.getId())
                    .name(home.getName())
                    .type(categoriesMap.getOrDefault(home.getCategoryId(), "Homestay"))
                    .location(locationsMap.getOrDefault(home.getLocationId(), "Chưa cập nhật"))
                    .image(mediaUtil.toCdnUrl(coverImage))
                    .price(basePrice)
                    .status(home.getStatus() != null ? home.getStatus() : HomestayStatus.AVAILABLE)
                    .stats(PropertyStats.builder()
                            .rating(rating)
                            .reviews(reviews)
                            .occupancy(occupancy)
                            .build())
                    .build();
        }).toList();
    }

    @Override
    public HostPortfolioSummaryResponse getPortfolioSummary(Long hostId) {
        log.info("[PORTFOLIO] Calculating REAL summary report for host ID: {}", hostId);

        List<Homestay> homestays = homestayRepository.findAllByOwnerId(hostId);
        int totalProperties = homestays.size();

        if (totalProperties == 0) {
            return HostPortfolioSummaryResponse.builder()
                    .totalPortfolioValue(BigDecimal.ZERO).portfolioGrowthRate(0.0)
                    .averageOccupancyRate(0.0).occupancyTrend("N/A")
                    .averageRating(0.0).ratingGrowth(0.0)
                    .totalProperties(0).build();
        }

        List<Long> homeIds = homestays.stream().map(Homestay::getId).toList();
        List<HomestayRoom> allRooms = homestayRoomService.findAllByIdIn(homeIds);
        List<Long> allRoomIds = allRooms.stream().map(HomestayRoom::getId).toList();

        // ==========================================
        // 1. TÍNH TOÁN DOANH THU & TĂNG TRƯỞNG (REVENUE)
        // ==========================================
        OffsetDateTime now = OffsetDateTime.now();
        OffsetDateTime startOfThisMonth = now.withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0);
        OffsetDateTime endOfThisMonth = now.withDayOfMonth(now.toLocalDate().lengthOfMonth()).withHour(23).withMinute(59).withSecond(59);

        OffsetDateTime startOfLastMonth = startOfThisMonth.minusMonths(1);
        OffsetDateTime endOfLastMonth = startOfThisMonth.minusSeconds(1);

        BigDecimal currentRevenue = bookingService.sumRevenueByHomestaysAndDateRange(homeIds, startOfThisMonth, endOfThisMonth);
        BigDecimal lastMonthRevenue = bookingService.sumRevenueByHomestaysAndDateRange(homeIds, startOfLastMonth, endOfLastMonth);

        double portfolioGrowthRate = 0.0;
        if (lastMonthRevenue.compareTo(BigDecimal.ZERO) > 0) {
            // Công thức: ((Tháng này - Tháng trước) / Tháng trước) * 100
            BigDecimal growth = currentRevenue.subtract(lastMonthRevenue)
                    .divide(lastMonthRevenue, 4, RoundingMode.HALF_UP)
                    .multiply(new BigDecimal("100"));
            portfolioGrowthRate = growth.setScale(1, RoundingMode.HALF_UP).doubleValue();
        } else if (currentRevenue.compareTo(BigDecimal.ZERO) > 0) {
            portfolioGrowthRate = 100.0; // Từ 0 lên có doanh thu tính là tăng 100%
        }

        // ==========================================
        // 2. TÍNH TOÁN LẤP ĐẦY & XU HƯỚNG (OCCUPANCY)
        // ==========================================
        YearMonth currentYearMonth = YearMonth.now();
        YearMonth lastYearMonth = currentYearMonth.minusMonths(1);

        List<BookingTimelineProjection> currentBookings = new ArrayList<>();
        List<BookingTimelineProjection> lastMonthBookings = new ArrayList<>();

        if (!allRoomIds.isEmpty()) {
            currentBookings = bookingDetailService.findOverlappingBookings(allRoomIds, currentYearMonth.atDay(1), currentYearMonth.atEndOfMonth());
            lastMonthBookings = bookingDetailService.findOverlappingBookings(allRoomIds, lastYearMonth.atDay(1), lastYearMonth.atEndOfMonth());
        }

        // Chạy thuật toán Batch 2 lần cho 2 tháng
        Map<Long, Integer> currentOccupancyMap = calculateBatchOccupancy(homeIds, allRooms, currentBookings, currentYearMonth.atDay(1), currentYearMonth.atEndOfMonth());
        Map<Long, Integer> lastMonthOccupancyMap = calculateBatchOccupancy(homeIds, allRooms, lastMonthBookings, lastYearMonth.atDay(1), lastYearMonth.atEndOfMonth());

        double avgOccupancy = currentOccupancyMap.values().stream().mapToInt(Integer::intValue).average().orElse(0.0);
        double prevAvgOccupancy = lastMonthOccupancyMap.values().stream().mapToInt(Integer::intValue).average().orElse(0.0);

        // Xác định xu hướng (Chênh lệch > 2% mới tính là tăng/giảm)
        String occupancyTrend = "Ổn định";
        if (avgOccupancy > prevAvgOccupancy + 2.0) occupancyTrend = "Tăng trưởng";
        else if (avgOccupancy < prevAvgOccupancy - 2.0) occupancyTrend = "Giảm nhẹ";

        // ==========================================
        // 3. TÍNH TOÁN RATING & TĂNG TRƯỞNG (RATING)
        // ==========================================
        // Lấy điểm trung bình tích luỹ tới cuối tháng này
        Double currentAvgRating = reviewService.getAverageRatingByHomestaysUpToDate(homeIds, endOfThisMonth);
        // Lấy điểm trung bình tích luỹ tới cuối tháng trước
        Double prevAvgRating = reviewService.getAverageRatingByHomestaysUpToDate(homeIds, endOfLastMonth);

        // Đảm bảo không bị null
        double avgRatingVal = currentAvgRating != null ? currentAvgRating : 0.0;
        double prevAvgRatingVal = prevAvgRating != null ? prevAvgRating : 0.0;

        // Tính mức độ tăng trưởng điểm đánh giá
        double ratingGrowth = 0.0;
        if (avgRatingVal > 0 && prevAvgRatingVal > 0) {
            ratingGrowth = avgRatingVal - prevAvgRatingVal;
        } else if (avgRatingVal > 0 && prevAvgRatingVal == 0) {
            ratingGrowth = avgRatingVal; // Có review đầu tiên
        }

        // ==========================================
        // 4. ĐÓNG GÓI RESPONSE
        // ==========================================
        return HostPortfolioSummaryResponse.builder()
                .totalPortfolioValue(currentRevenue)
                .portfolioGrowthRate(portfolioGrowthRate)
                .averageOccupancyRate(Math.round(avgOccupancy * 10.0) / 10.0)
                .occupancyTrend(occupancyTrend)
                .averageRating(Math.round(avgRatingVal * 100.0) / 100.0)
                .ratingGrowth(Math.round(ratingGrowth * 100.0) / 100.0)
                .totalProperties(totalProperties)
                .build();
    }

    @Override
    public Long getOwnerIdByHomestayId(Long homestayId) {
        return homestayRepository.getOwnerIdByHomestayId(homestayId);
    }

    private List<GlobalSearchResponse> mapHomestaysToResponse(List<Homestay> homestays) {
        if (homestays.isEmpty()) return List.of();

        List<Long> ids = homestays.stream().map(Homestay::getId).toList();
        var roomMap = homestayRoomService.getRoomSummaries(ids).stream()
                .collect(Collectors.toMap(s -> s.getHomestayId(), s -> s));
        var imagesMap = homestayImageService.getImagesForHomestays(ids);
        var locationIds = homestays.stream().map(Homestay::getLocationId).distinct().toList();
        var locationsMap = locationService.getLocationNamesMap(locationIds);

        return homestays.stream().map(h -> {
            var room = roomMap.get(h.getId());
            return new GlobalSearchResponse(
                    h.getId(), h.getName(),
                    locationsMap.get(h.getLocationId()),
                    room != null ? room.getMinPrice() : BigDecimal.ZERO,
                    imagesMap.getOrDefault(h.getId(), List.of()),
                    "HOMESTAY",
                    h.getAverageRating() != null ? h.getAverageRating().doubleValue() : 0.0,
                    room != null ? room.getMaxGuestsInRoom() : 0,
                    room != null ? room.getTotalRooms() : 0
            );
        }).toList();
    }

    private List<GlobalSearchResponse> mapToursToResponse(List<Tour> tours) {
        if (tours.isEmpty()) return List.of();

        List<Long> ids = tours.stream().map(Tour::getId).toList();
        var imagesMap = tourImageService.getImagesForTours(ids);

        return tours.stream().map(t -> new GlobalSearchResponse(
                t.getId(), t.getName(),
                t.getLocationDetail(), // Tour lấy trực tiếp từ location_detail
                t.getPricePerPerson(),
                imagesMap.getOrDefault(t.getId(), List.of()),
                "TOUR",
                5.0, // Tạm fix 5 sao hoặc lấy từ bảng tour review nếu bác có
                t.getMaxParticipants(),
                0 // Tour không có bedrooms
        )).toList();
    }
    private RoomTimelineResponse mapToRoomTimeline(
            HomestayRoom room,
            Map<Long, List<RoomCalendar>> calendarMap,
            Map<Long, List<BookingTimelineProjection>> bookingMap,
            Map<Long,String> roomImageMap,
            Map<Long,String>  ownerResponseMap
    ) {
        // 1. Map dữ liệu Giá theo ngày
        List<DailyStatusResponse> dailyStatuses = calendarMap.getOrDefault(room.getId(), List.of())
                .stream()
                .map(c -> DailyStatusResponse.builder()
                        .date(c.getNightDate())
                        .price(c.getPriceOverride() != null ? c.getPriceOverride() : BigDecimal.ZERO)
                        .availableQuantity(c.getAvailableQuantity())
                        .build())
                .toList();

        // 2. Map dữ liệu Đặt phòng (Các khối đen Marcus T, Sarah K,...)
        List<BookingBlockResponse> bookingBlocks = bookingMap.getOrDefault(room.getId(), List.of())
                .stream()
                .map(b -> BookingBlockResponse.builder()
                        .bookingId(b.bookingId())
                        .guestName(b.guestName())
                        .avatar(ownerResponseMap.get(b.userId()))
                        .checkInDate(b.checkInDate())
                        .checkOutDate(b.checkOutDate())
                        .status(b.status())
                        .build())
                .toList();

        // 3. Trả về DTO hoàn chỉnh
        return RoomTimelineResponse.builder()
                .roomId(room.getId())
                .roomName(room.getName())
                .dailyStatuses(dailyStatuses)
                .bookings(bookingBlocks)
                .imageUrl(roomImageMap.get(room.getId()))
                .build();
    }
    /**
     * Thuật toán gom nhóm để tính tỷ lệ lấp đầy thực tế cho hàng loạt Homestay
     */
    private Map<Long, Integer> calculateBatchOccupancy(
            List<Long> homeIds,
            List<HomestayRoom> allRooms,
            List<BookingTimelineProjection> bookings,
            LocalDate startOfMonth,
            LocalDate endOfMonth) {

        int daysInMonth = startOfMonth.lengthOfMonth();
        Map<Long, Integer> resultMap = new HashMap<>();

        // Group 1: Gom phòng theo homestayId
        Map<Long, List<HomestayRoom>> roomsByHomeMap = allRooms.stream()
                .collect(Collectors.groupingBy(HomestayRoom::getHomestayId));

        // Group 2: Gom booking theo roomId
        Map<Long, List<BookingTimelineProjection>> bookingsByRoom = bookings.stream()
                .collect(Collectors.groupingBy(BookingTimelineProjection::roomId));

        for (Long homeId : homeIds) {
            List<HomestayRoom> rooms = roomsByHomeMap.getOrDefault(homeId, List.of());
            if (rooms.isEmpty()) {
                resultMap.put(homeId, 0);
                continue;
            }

            int totalCapacityNights = 0;
            int totalBookedNights = 0;

            for (HomestayRoom room : rooms) {
                int roomQuantity = room.getQuantity() != null ? room.getQuantity() : 1;
                // A. Tính tổng số đêm có thể bán của loại phòng này trong tháng
                totalCapacityNights += roomQuantity * daysInMonth;

                // B. Tính số đêm đã bị khách đặt
                List<BookingTimelineProjection> roomBookings = bookingsByRoom.getOrDefault(room.getId(), List.of());
                for (BookingTimelineProjection b : roomBookings) {
                    if (BookingStatus.CANCELLED.equals(b.status()) || BookingStatus.FAILED.equals(b.status())) {
                        continue;
                    }

                    // Chặn ngày (cắt bớt những ngày nằm ngoài tháng hiện tại để tính chính xác)
                    LocalDate checkIn = b.checkInDate().isBefore(startOfMonth) ? startOfMonth : b.checkInDate();
                    LocalDate checkOut = b.checkOutDate().isAfter(endOfMonth.plusDays(1)) ? endOfMonth.plusDays(1) : b.checkOutDate();

                    long nights = java.time.temporal.ChronoUnit.DAYS.between(checkIn, checkOut);
                    if (nights > 0) {

                        int qty = b.quantity();
                        totalBookedNights += (int) nights * qty;
                    }
                }
            }

            // C. Chia tỷ lệ và gán vào kết quả
            if (totalCapacityNights == 0) {
                resultMap.put(homeId, 0);
            } else {
                int occupancy = (int) Math.round((totalBookedNights * 100.0) / totalCapacityNights);
                resultMap.put(homeId, Math.min(100, occupancy)); // Khống chế trần 100% để đề phòng overbooking
            }
        }

        return resultMap;
    }
}