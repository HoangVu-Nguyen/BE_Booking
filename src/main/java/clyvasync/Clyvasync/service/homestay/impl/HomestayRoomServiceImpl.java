package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.dto.projection.RoomAvailabilityProjection;
import clyvasync.Clyvasync.dto.projection.RoomImageProjection;
import clyvasync.Clyvasync.dto.request.*;
import clyvasync.Clyvasync.dto.response.*;
import clyvasync.Clyvasync.dto.summary.HomestayRoomSummary;
import clyvasync.Clyvasync.enums.media.MediaStatus;
import clyvasync.Clyvasync.enums.room.BedType;
import clyvasync.Clyvasync.enums.room.RoomStatus;
import clyvasync.Clyvasync.enums.room.RoomType;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.mapper.room.RoomMapper;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayRoom;
import clyvasync.Clyvasync.modules.room.*;
import clyvasync.Clyvasync.repository.homestay.HomestayRoomRepository;
import clyvasync.Clyvasync.repository.room.*;
import clyvasync.Clyvasync.service.homestay.HomestayRoomService;
import clyvasync.Clyvasync.service.media.S3Service;
import clyvasync.Clyvasync.service.room.RatePlanBenefitMappingService;
import clyvasync.Clyvasync.service.room.RoomAmenityHighlightService;
import clyvasync.Clyvasync.service.room.RoomRatePlanService;
import clyvasync.Clyvasync.utils.MediaUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Optional.ofNullable;

@Service
@RequiredArgsConstructor
public class HomestayRoomServiceImpl implements HomestayRoomService {

    private final HomestayRoomRepository roomRepository;
    private final RoomRatePlanService roomRatePlanService;
    private final RoomMapper roomMapper;
    private final RoomAmenityHighlightService roomAmenityHighlightService;
    private final RatePlanBenefitMappingService ratePlanBenefitMappingService;
    private final RoomRatePlanRepository roomRatePlanRepository;
    private final RoomBedRepository roomBedRepository;
    private final RoomImageRepository roomImageRepository;
    private final RoomCalendarRepository roomCalendarRepository;
    private final RatePlanCalendarRepository ratePlanCalendarRepository;
    private final MediaUtil mediaUtil;
    private final S3Service s3Service;
    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> getAllRoomsByHomestay(Long homestayId) {
        List<HomestayRoom> rooms = roomRepository.findAllByHomestayIdAndStatus(
                homestayId,
                RoomStatus.ACTIVE
        );

        Map<Long, Integer> availableCountMap = rooms.stream()
                .collect(Collectors.toMap(
                        HomestayRoom::getId,
                        HomestayRoom::getQuantity
                ));

        return processRoomResponses(rooms, availableCountMap);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomResponse> findAvailableRooms(
            Long homestayId,
            LocalDate checkIn,
            LocalDate checkOut,
            int guests
    ) {
        List<RoomAvailabilityProjection> projections =
                roomRepository.findAvailableRoomsProjections(
                        homestayId,
                        checkIn,
                        checkOut,
                        guests
                );

        if (projections.isEmpty()) {
            return List.of();
        }

        Map<Long, Integer> availableCountMap = projections.stream()
                .collect(Collectors.toMap(
                        RoomAvailabilityProjection::getId,
                        RoomAvailabilityProjection::getAvailableQty
                ));

        List<Long> roomIds = projections.stream()
                .map(RoomAvailabilityProjection::getId)
                .toList();

        List<HomestayRoom> rooms = roomRepository.findAllById(roomIds);
        List<RoomBed> roomBeds = roomBedRepository.findByRoomIdIn(roomIds);
        List<RoomImage> roomImages = roomImageRepository.findByRoomIdIn(roomIds);
        List<RoomCalendar> roomCalendars =
                roomCalendarRepository.findCalendarsByRoomIdsAndDateRange(
                        roomIds,
                        checkIn,
                        checkOut
                );

        return processRoomResponses(
                rooms,
                availableCountMap,
                roomBeds,
                roomImages,
                roomCalendars,
                checkIn,
                checkOut
        );
    }

    private List<RoomResponse> processRoomResponses(
            List<HomestayRoom> rooms,
            Map<Long, Integer> availableCountMap
    ) {
        if (rooms.isEmpty()) {
            return List.of();
        }

        List<Long> roomIds = rooms.stream()
                .map(HomestayRoom::getId)
                .toList();

        List<RoomBed> roomBeds = roomBedRepository.findByRoomIdIn(roomIds);
        List<RoomImage> roomImages = roomImageRepository.findByRoomIdIn(roomIds);

        return processRoomResponses(
                rooms,
                availableCountMap,
                roomBeds,
                roomImages,
                List.of(),
                null,
                null
        );
    }

    private List<RoomResponse> processRoomResponses(
            List<HomestayRoom> rooms,
            Map<Long, Integer> availableCountMap,
            List<RoomBed> roomBeds,
            List<RoomImage> roomImages,
            List<RoomCalendar> roomCalendars,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        if (rooms.isEmpty()) {
            return List.of();
        }

        List<Long> roomIds = rooms.stream()
                .map(HomestayRoom::getId)
                .toList();

        List<RoomRatePlan> ratePlans = roomRatePlanService.getAllRoomRatePlans(roomIds);

        List<Long> ratePlanIds = ratePlans.stream()
                .map(RoomRatePlan::getId)
                .toList();

        Map<Long, List<AmenityHighlightResponse>> highlightsMap =
                roomAmenityHighlightService.getHighlightsForRooms(roomIds);

        Map<Long, List<String>> benefitsMap =
                ratePlanBenefitMappingService.findBenefitsByPlanIds(ratePlanIds);

        Map<Long, Map<LocalDate, RatePlanCalendar>> ratePlanCalendarMap =
                getRatePlanCalendarMap(ratePlanIds, checkIn, checkOut);

        Map<Long, List<RatePlanResponse>> ratePlanResponseMap = ratePlans.stream()
                .collect(Collectors.groupingBy(
                        RoomRatePlan::getRoomId,
                        Collectors.mapping(
                                plan -> toRatePlanResponse(
                                        plan,
                                        benefitsMap.getOrDefault(plan.getId(), List.of()),
                                        ratePlanCalendarMap.getOrDefault(plan.getId(), Map.of()),
                                        checkIn,
                                        checkOut
                                ),
                                Collectors.toList()
                        )
                ));

        Map<Long, List<BedResponse>> bedsMap = roomBeds.stream()
                .collect(Collectors.groupingBy(
                        RoomBed::getRoomId,
                        Collectors.mapping(this::toBedResponse, Collectors.toList())
                ));

        Map<Long, List<RoomImageResponse>> imagesMap = roomImages.stream()
                .collect(Collectors.groupingBy(
                        RoomImage::getRoomId,
                        Collectors.mapping(this::toRoomImageResponse, Collectors.toList())
                ));

        Map<Long, List<CalendarInventoryResponse>> inventoryMap = roomCalendars.stream()
                .collect(Collectors.groupingBy(
                        RoomCalendar::getRoomId,
                        Collectors.mapping(this::toCalendarInventoryResponse, Collectors.toList())
                ));

        return rooms.stream()
                .map(room -> {
                    RoomResponse response = roomMapper.toRoomResponse(room);

                    List<RatePlanResponse> roomRatePlans =
                            ratePlanResponseMap.getOrDefault(room.getId(), List.of());

                    response.setHighlights(highlightsMap.getOrDefault(room.getId(), List.of()));
                    response.setRatePlans(roomRatePlans);
                    response.setBeds(bedsMap.getOrDefault(room.getId(), List.of()));
                    response.setImages(imagesMap.getOrDefault(room.getId(), List.of()));
                    response.setInventory(inventoryMap.getOrDefault(room.getId(), List.of()));
                    response.setAvailableQuantity(
                            availableCountMap.getOrDefault(room.getId(), 0)
                    );
                    response.setBasePrice(getLowestPriceFromRatePlans(roomRatePlans));

                    return response;
                })
                .toList();
    }
    private Map<Long, Map<LocalDate, RatePlanCalendar>> getRatePlanCalendarMap(
            List<Long> ratePlanIds,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        if (ratePlanIds == null || ratePlanIds.isEmpty()) {
            return Map.of();
        }

        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return Map.of();
        }

        LocalDate priceStart = checkIn;
        LocalDate priceEnd = checkOut.minusDays(1);

        List<RatePlanCalendar> calendars =
                ratePlanCalendarRepository.findByRatePlanIdInAndNightDateBetween(
                        ratePlanIds,
                        priceStart,
                        priceEnd
                );

        return calendars.stream()
                .collect(Collectors.groupingBy(
                        RatePlanCalendar::getRatePlanId,
                        Collectors.toMap(
                                RatePlanCalendar::getNightDate,
                                calendar -> calendar,
                                (oldValue, newValue) -> newValue
                        )
                ));
    }
    @Override
    @Transactional(readOnly = true)
    public List<HomestayRoomSummary> getRoomSummaries(List<Long> homestayIds) {
        return roomRepository.getRoomSummaries(homestayIds);
    }

    @Override
    @Transactional(readOnly = true)
    public HomestayRoom getRoomById(Long roomId) {
        return roomRepository.findById(roomId)
                .orElseThrow(() -> new AppException(ResultCode.HOMESTAY_NOT_FOUND));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HomestayRoom> findByIdIn(List<Long> roomIds) {
        return roomRepository.findByIdIn(roomIds);
    }

    @Override
    @Transactional(readOnly = true)
    public List<HomestayRoom> findAllById(Long homestayId) {
        return roomRepository.findById(homestayId)
                .stream()
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<HomestayRoom> findAllByIdIn(List<Long> homestayIds) {
        return roomRepository.findAllByHomestayIdIn(homestayIds);
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, String> getRoomImageMap(List<Long> roomIds) {
        List<RoomImageProjection> projections =
                roomRepository.findRoomImagesByIdIn(roomIds);

        return projections.stream()
                .collect(Collectors.toMap(
                        RoomImageProjection::getRoomId,
                        RoomImageProjection::getImageUrl,
                        (existing, replacement) -> existing
                ));
    }

    @Override
    @Transactional(readOnly = true)
    public List<HomestayRoom> findAllByHomestayIdAndStatus(
            Long homestayId,
            RoomStatus status
    ) {
        return roomRepository.findAllByHomestayIdAndStatus(homestayId, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomDisplayResponse> getRoomsByHomestayId(Long homestayId) {
        List<HomestayRoom> rooms = roomRepository.findAllByHomestayId(homestayId);

        if (rooms.isEmpty()) {
            return List.of();
        }

        List<Long> roomIds = rooms.stream()
                .map(HomestayRoom::getId)
                .toList();

        Map<Long, List<RoomBed>> bedsMap = roomBedRepository.findByRoomIdIn(roomIds)
                .stream()
                .collect(Collectors.groupingBy(RoomBed::getRoomId));

        Map<Long, List<RoomImage>> imagesMap = roomImageRepository.findByRoomIdIn(roomIds)
                .stream()
                .collect(Collectors.groupingBy(RoomImage::getRoomId));

        Map<Long, List<RoomRatePlan>> ratePlansMap =
                roomRatePlanRepository.findByRoomIdIn(roomIds)
                        .stream()
                        .collect(Collectors.groupingBy(RoomRatePlan::getRoomId));

        return rooms.stream()
                .map(room -> buildRoomDisplayResponse(
                        room,
                        bedsMap.getOrDefault(room.getId(), List.of()),
                        imagesMap.getOrDefault(room.getId(), List.of()),
                        ratePlansMap.getOrDefault(room.getId(), List.of())
                ))
                .toList();
    }

    private RoomDisplayResponse buildRoomDisplayResponse(
            HomestayRoom room,
            List<RoomBed> beds,
            List<RoomImage> images,
            List<RoomRatePlan> ratePlans
    ) {
        return RoomDisplayResponse.builder()
                .id(room.getId())
                .name(room.getName())
                .type(room.getType())
                .description(room.getDescription())
                .maxGuests(room.getMaxGuests())
                .area(room.getArea())
                .hasPrivateBathroom(room.getHasPrivateBathroom())
                .price(getLowestPriceFromRatePlanEntities(ratePlans))
                .beds(toBedResponses(beds))
                .images(toRoomImageResponses(images))
                .ratePlans(toRatePlanResponses(ratePlans))
                .build();
    }

    private List<BedResponse> toBedResponses(List<RoomBed> beds) {
        return beds.stream()
                .map(this::toBedResponse)
                .toList();
    }

    private BedResponse toBedResponse(RoomBed bed) {
        return BedResponse.builder()
                .id(bed.getId())
                .type(bed.getBedType())
                .quantity(bed.getQuantity())
                .build();
    }

    private List<RoomImageResponse> toRoomImageResponses(List<RoomImage> images) {
        return images.stream()
                .map(this::toRoomImageResponse)
                .toList();
    }

    private RoomImageResponse toRoomImageResponse(RoomImage image) {
        return RoomImageResponse.builder()
                .id(image.getId())
                .url(mediaUtil.toCdnUrl(image.getImageUrl()))
                .isCover(image.getIsCover())
                .displayOrder(image.getDisplayOrder())
                .build();
    }

    private List<RatePlanResponse> toRatePlanResponses(List<RoomRatePlan> ratePlans) {
        return ratePlans.stream()
                .map(ratePlan -> toRatePlanResponse(ratePlan, List.of()))
                .toList();
    }

    private RatePlanResponse toRatePlanResponse(
            RoomRatePlan ratePlan,
            List<String> benefits
    ) {
        return RatePlanResponse.builder()
                .id(ratePlan.getId())
                .name(ratePlan.getName())
                .price(ratePlan.getPrice())
                .isNonRefundable(ratePlan.getIsNonRefundable())
                .benefits(benefits)
                .build();
    }

    private CalendarInventoryResponse toCalendarInventoryResponse(RoomCalendar calendar) {
        return CalendarInventoryResponse.builder()
                .date(calendar.getNightDate())
                .availableQuantity(calendar.getAvailableQuantity())
                .build();
    }

    private BigDecimal getLowestPriceFromRatePlans(List<RatePlanResponse> ratePlans) {
        return ratePlans.stream()
                .map(RatePlanResponse::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }

    private BigDecimal getLowestPriceFromRatePlanEntities(List<RoomRatePlan> ratePlans) {
        return ratePlans.stream()
                .map(RoomRatePlan::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);
    }
    private RatePlanResponse toRatePlanResponse(
            RoomRatePlan ratePlan,
            List<String> benefits,
            Map<LocalDate, RatePlanCalendar> priceOverrides,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        BigDecimal effectiveNightPrice = calculateAverageNightPrice(
                ratePlan,
                priceOverrides,
                checkIn,
                checkOut
        );

        return RatePlanResponse.builder()
                .id(ratePlan.getId())
                .name(ratePlan.getName())
                .price(effectiveNightPrice)
                .isNonRefundable(ratePlan.getIsNonRefundable())
                .benefits(benefits)
                .build();
    }
    private BigDecimal calculateAverageNightPrice(
            RoomRatePlan ratePlan,
            Map<LocalDate, RatePlanCalendar> priceOverrides,
            LocalDate checkIn,
            LocalDate checkOut
    ) {
        BigDecimal basePrice = ratePlan.getPrice() != null
                ? ratePlan.getPrice()
                : BigDecimal.ZERO;

        if (checkIn == null || checkOut == null || !checkOut.isAfter(checkIn)) {
            return basePrice;
        }

        long nights = ChronoUnit.DAYS.between(checkIn, checkOut);

        if (nights <= 0) {
            return basePrice;
        }

        BigDecimal totalPrice = BigDecimal.ZERO;

        for (LocalDate date = checkIn; date.isBefore(checkOut); date = date.plusDays(1)) {
            RatePlanCalendar override = priceOverrides.get(date);

            BigDecimal nightlyPrice = override != null && override.getPrice() != null
                    ? override.getPrice()
                    : basePrice;

            totalPrice = totalPrice.add(nightlyPrice);
        }

        return totalPrice.divide(
                BigDecimal.valueOf(nights),
                2,
                RoundingMode.HALF_UP
        );
    }


    private String getFileExtension(String fileName) {
        if (fileName == null || !fileName.contains(".")) return "jpg"; // Default
        return fileName.substring(fileName.lastIndexOf(".") + 1);
    }
    @Override
    @Transactional
    public void updateRooms(Long ownerId, RoomBatchUpdateRequest request) {
        System.out.println(request);
        validateRequest(ownerId, request);

        for (RoomUpdateRequest roomReq : request.getRooms()) {
            HomestayRoom room = createOrUpdateRoom(roomReq, request.getHomestayId());

            updateRoomBeds(room.getId(), roomReq.getBeds());

            processRoomImages(room.getId(), roomReq.getImages());
        }

    }
    private void validateRequest(Long ownerId, RoomBatchUpdateRequest request) {
        if (request == null || CollectionUtils.isEmpty(request.getRooms())) {
            throw new AppException(ResultCode.INVALID_INPUT);
        }
        if (request.getHomestayId() == null || ownerId == null) {
            throw new AppException(ResultCode.INVALID_INPUT);
        }
        // Optional: Check ownership
        // homestayService.validateOwnership(ownerId, request.getHomestayId());
    }
    /**
     * Hàm tách lọc và xử lý mảng gộp Images
     */
    private void processRoomImages(Long roomId, List<ImageSubmitRequest> imageRequests) {
        System.out.println("processRoomImages roomId = " + roomId);
        System.out.println("processRoomImages imageRequests = " + imageRequests);

        if (CollectionUtils.isEmpty(imageRequests)) {
            roomImageRepository.deleteByRoomId(roomId);
            return;
        }

        List<ImageSubmitRequest> oldImagesReq = new ArrayList<>();
        List<ImageSubmitRequest> newImagesReq = new ArrayList<>();

        for (ImageSubmitRequest req : imageRequests) {
            if (req.getId() != null) {
                oldImagesReq.add(req);
            } else if (req.getObjectKey() != null && !req.getObjectKey().isBlank()) {
                newImagesReq.add(req);
            }
        }

        Set<Long> keepImageIds = oldImagesReq.stream()
                .map(ImageSubmitRequest::getId)
                .collect(Collectors.toSet());

        List<String> newObjectKeys = newImagesReq.stream()
                .map(ImageSubmitRequest::getObjectKey)
                .filter(Objects::nonNull)
                .filter(key -> !key.isBlank())
                .distinct()
                .toList();

        List<RoomImage> pendingImages = newObjectKeys.isEmpty()
                ? List.of()
                : roomImageRepository.findByRoomIdAndImageUrlIn(roomId, newObjectKeys);

        Set<Long> pendingImageIds = pendingImages.stream()
                .map(RoomImage::getId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Set<Long> allKeepIds = new HashSet<>();
        allKeepIds.addAll(keepImageIds);
        allKeepIds.addAll(pendingImageIds);

        if (allKeepIds.isEmpty()) {
            roomImageRepository.deleteByRoomId(roomId);
        } else {
            roomImageRepository.deleteByRoomIdAndIdNotIn(roomId, new ArrayList<>(allKeepIds));
        }

        List<RoomImage> imagesToSave = new ArrayList<>();

        if (!oldImagesReq.isEmpty()) {
            List<RoomImage> existingImages = roomImageRepository.findByRoomIdAndIdIn(
                    roomId,
                    new ArrayList<>(keepImageIds)
            );

            for (RoomImage img : existingImages) {
                oldImagesReq.stream()
                        .filter(req -> req.getId().equals(img.getId()))
                        .findFirst()
                        .ifPresent(req -> {
                            img.setIsCover(Boolean.TRUE.equals(req.getIsCover()));
                            img.setDisplayOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
                        });

                imagesToSave.add(img);
            }
        }

        if (!pendingImages.isEmpty()) {
            for (RoomImage img : pendingImages) {
                newImagesReq.stream()
                        .filter(req -> req.getObjectKey().equals(img.getImageUrl()))
                        .findFirst()
                        .ifPresent(req -> {
                            img.setStatus(MediaStatus.ACTIVE);
                            img.setIsCover(Boolean.TRUE.equals(req.getIsCover()));
                            img.setDisplayOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
                            img.setRoomId(roomId);
                        });

                imagesToSave.add(img);
            }
        }

        normalizeCoverImage(imagesToSave);

        if (!imagesToSave.isEmpty()) {
            List<RoomImage> savedImages = roomImageRepository.saveAllAndFlush(imagesToSave);

            System.out.println("saved room images size = " + savedImages.size());
            savedImages.forEach(img -> System.out.println(
                    "saved image id=" + img.getId()
                            + ", roomId=" + img.getRoomId()
                            + ", imageUrl=" + img.getImageUrl()
                            + ", status=" + img.getStatus()
                            + ", isCover=" + img.getIsCover()
            ));
        }
    }
    private void normalizeCoverImage(List<RoomImage> images) {
        if (CollectionUtils.isEmpty(images)) {
            return;
        }

        List<RoomImage> sortedImages = images.stream()
                .sorted(Comparator.comparing(
                        image -> image.getDisplayOrder() == null ? 999 : image.getDisplayOrder()
                ))
                .toList();

        boolean hasCover = sortedImages.stream()
                .anyMatch(image -> Boolean.TRUE.equals(image.getIsCover()));

        if (!hasCover) {
            sortedImages.get(0).setIsCover(true);
            return;
        }

        boolean firstCoverFound = false;

        for (RoomImage image : sortedImages) {
            if (Boolean.TRUE.equals(image.getIsCover())) {
                if (!firstCoverFound) {
                    firstCoverFound = true;
                } else {
                    image.setIsCover(false);
                }
            }
        }
    }
    @Override
    @Transactional
    public List<PresignedUrlResponse> prepareHomestayRoomImageBatch(Long ownerId, MultiRoomBatchUploadRequest request) {

        if (request == null || CollectionUtils.isEmpty(request.getRooms())) {
            return List.of();
        }

        List<RoomImage> allNewImages = new ArrayList<>();
        List<PresignedUrlResponse> responseList = new ArrayList<>();

        for (RoomImageBatch roomBatch : request.getRooms()) {
            Long roomId = roomBatch.getRoomId();

            if (CollectionUtils.isEmpty(roomBatch.getItems())) {
                continue;
            }

            // (Tùy chọn) Validate: Check ownerId có quyền với roomId này không ở đây

            // Vòng lặp 2: Duyệt qua từng ảnh của phòng đó
            for (UploadRequest item : roomBatch.getItems()) {

                // Sinh key: rooms/{roomId}/{uuid}.{ext}
                String objectKey = mediaUtil.generateObjectKey(ownerId,item );

                // Add vào list Entity để lát Save 1 cục
                RoomImage newImage = RoomImage.builder()
                        .roomId(roomId)
                        .imageUrl(objectKey)
                        .isCover(item.getIsCover() != null ? item.getIsCover() : false)
                        .displayOrder(item.getSortOrder() != null ? item.getSortOrder() : 0)
                        .status(MediaStatus.PENDING)
                        .build();
                allNewImages.add(newImage);

                // Xin link S3
                String contentType = item.getContentType() != null ? item.getContentType() : getContentType(item.getFileName());
                String presignedUrl = s3Service.generatePresignedPutUrl(
                        objectKey,
                        contentType,
                        item.getFileSize()
                );

                responseList.add(PresignedUrlResponse.builder()
                        .roomId(roomId)
                        .fileName(item.getFileName())
                        .objectKey(objectKey)
                        .uploadUrl(presignedUrl)
                        .build());
            }
        }
        System.out.println( "allNewImages " + allNewImages);
        if (!allNewImages.isEmpty()) {
            System.out.println(roomImageRepository.saveAll(allNewImages));
        }

        return responseList;
    }

    private HomestayRoom createOrUpdateRoom(RoomUpdateRequest roomReq, Long homestayId) {
        HomestayRoom room;

        if (roomReq.getId() != null) {
            room = roomRepository.findById(roomReq.getId())
                    .orElseThrow(() -> new AppException(ResultCode.ROOM_NOT_FOUND));
        } else {
            room = HomestayRoom.builder()
                    .homestayId(homestayId)
                    .status(RoomStatus.ACTIVE)
                    .build();
        }

        // Update fields
        ofNullable(roomReq.getName()).ifPresent(room::setName);
        ofNullable(roomReq.getType()).ifPresent(name -> room.setType(RoomType.valueOf(name)));
        ofNullable(roomReq.getDescription()).ifPresent(room::setDescription);
        ofNullable(roomReq.getMaxGuests()).ifPresent(room::setMaxGuests);
        ofNullable(roomReq.getArea()).ifPresent(room::setArea);
        ofNullable(roomReq.getHasPrivateBathroom()).ifPresent(room::setHasPrivateBathroom);


        return roomRepository.save(room);
    }
    private void updateRoomBeds(Long roomId, List<BedRequest> bedRequests) {
        // Delete old beds
        roomBedRepository.deleteByRoomId(roomId);

        if (CollectionUtils.isEmpty(bedRequests)) {
            return;
        }

        // Create new beds
        List<RoomBed> newBeds = bedRequests.stream()
                .map(bedReq -> RoomBed.builder()
                        .roomId(roomId)
                        .bedType(bedReq.getType())
                        .quantity(bedReq.getQuantity())
                        .build()
                )
                .toList();

        roomBedRepository.saveAll(newBeds);
    }

    /**
     * Update existing images (keep, delete, reorder, update cover)updateRooms
     */
    private void updateExistingImages(Long roomId, List<ExistingImageRequest> existingImageRequests) {
        if (CollectionUtils.isEmpty(existingImageRequests)) {
            // Delete all images if no existing images provided
            roomImageRepository.deleteByRoomId(roomId);
            return;
        }

        // Get IDs to keep
        Set<Long> keepImageIds = existingImageRequests.stream()
                .map(ExistingImageRequest::getId)
                .collect(Collectors.toSet());

        // Delete images not in the keep list
        roomImageRepository.deleteByRoomIdAndIdNotIn(roomId, new ArrayList<>(keepImageIds));

        // Update sort order and cover flag for kept images
        List<RoomImage> imagesToUpdate = roomImageRepository.findByRoomId(roomId);
        imagesToUpdate.forEach(img -> {
            ExistingImageRequest req = existingImageRequests.stream()
                    .filter(r -> r.getId().equals(img.getId()))
                    .findFirst()
                    .orElse(null);

            if (req != null) {
                img.setIsCover(req.getIsCover());
                img.setDisplayOrder(req.getSortOrder());
            }
        });

        roomImageRepository.saveAll(imagesToUpdate);
    }

    /**
     * Process new images: create PENDING records and generate presigned URLs
     */
    private void processNewImages(Long roomId, Long userId, List<NewImageRequest> newImageRequests,
                                  List<PresignedUrlResponse> uploadUrls) {
        if (CollectionUtils.isEmpty(newImageRequests)) {
            return;
        }

        for (NewImageRequest newImgReq : newImageRequests) {
            // Generate object key
            String objectKey = generateRoomImageObjectKey(userId, roomId, newImgReq.getFileName());

            RoomImage newImage = RoomImage.builder()
                    .roomId(roomId)
                    .imageUrl(objectKey) // Lưu objectKey vào imageUrl
                    .isCover(newImgReq.getIsCover())
                    .displayOrder(newImgReq.getSortOrder())
                    .build();
            roomImageRepository.save(newImage);

            // Generate presigned URL from S3
            String presignedUrl = s3Service.generatePresignedPutUrl(
                    objectKey,
                    getContentType(newImgReq.getFileName()),
                    getFileSize(newImgReq)
            );

            uploadUrls.add(PresignedUrlResponse.builder()
                    .uploadUrl(presignedUrl)
                    .objectKey(objectKey)
                    .build());
        }
    }

    /**
     * Generate safe object key for room image: rooms/{roomId}/{uuid}.{ext}
     */
    private String generateRoomImageObjectKey(Long userId, Long roomId, String fileName) {
        String extension = getFileExtension(fileName);
        String uuid = UUID.randomUUID().toString();
        return String.format("rooms/%d/%s.%s", roomId, uuid, extension);
    }

    /**
     * Extract file extension from filename
     */


    /**
     * Get MIME type based on file extension
     */
    private String getContentType(String fileName) {
        String ext = getFileExtension(fileName).toLowerCase();
        return switch (ext) {
            case "jpg", "jpeg" -> "image/jpeg";
            case "png" -> "image/png";
            case "webp" -> "image/webp";
            case "gif" -> "image/gif";
            default -> "image/jpeg";
        };
    }

    /**
     * Get file size (dummy implementation - adjust based on your needs)
     */
    private Long getFileSize(NewImageRequest newImgReq) {
        return 5_242_880L; // 5MB default, adjust as needed
    }

}