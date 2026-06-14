package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.dto.projection.RoomAvailabilityProjection;
import clyvasync.Clyvasync.dto.projection.RoomImageProjection;
import clyvasync.Clyvasync.dto.response.AmenityHighlightResponse;
import clyvasync.Clyvasync.dto.response.BedResponse;
import clyvasync.Clyvasync.dto.response.CalendarInventoryResponse;
import clyvasync.Clyvasync.dto.response.RatePlanResponse;
import clyvasync.Clyvasync.dto.response.RoomDisplayResponse;
import clyvasync.Clyvasync.dto.response.RoomImageResponse;
import clyvasync.Clyvasync.dto.response.RoomResponse;
import clyvasync.Clyvasync.dto.summary.HomestayRoomSummary;
import clyvasync.Clyvasync.enums.room.RoomStatus;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.mapper.room.RoomMapper;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayRoom;
import clyvasync.Clyvasync.modules.room.*;
import clyvasync.Clyvasync.repository.homestay.HomestayRoomRepository;
import clyvasync.Clyvasync.repository.room.*;
import clyvasync.Clyvasync.service.homestay.HomestayRoomService;
import clyvasync.Clyvasync.service.room.RatePlanBenefitMappingService;
import clyvasync.Clyvasync.service.room.RoomAmenityHighlightService;
import clyvasync.Clyvasync.service.room.RoomRatePlanService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
                .areaM2(room.getArea())
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
                .url(image.getImageUrl())
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
}