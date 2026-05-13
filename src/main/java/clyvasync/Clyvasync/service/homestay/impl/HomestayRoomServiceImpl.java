package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.dto.response.AmenityHighlightResponse;
import clyvasync.Clyvasync.dto.response.AmenityResponse;
import clyvasync.Clyvasync.dto.response.RatePlanResponse;
import clyvasync.Clyvasync.dto.response.RoomResponse;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayRoom;
import clyvasync.Clyvasync.modules.homestay.entity.RatePlanBenefitMapping;
import clyvasync.Clyvasync.modules.homestay.entity.RoomAmenityHighlight;
import clyvasync.Clyvasync.modules.homestay.entity.RoomRatePlan;
import clyvasync.Clyvasync.repository.homestay.*;
import clyvasync.Clyvasync.service.homestay.HomestayRoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomestayRoomServiceImpl implements HomestayRoomService {
    private final HomestayRoomRepository roomRepository;
    private final AmenityRepository amenityRepository;
    private final RoomAmenityHighlightRepository highlightRepository;
    private final RoomRatePlanRepository ratePlanRepository;
    private final RatePlanBenefitMappingRepository benefitMappingRepository;

    @Override
    public List<RoomResponse> getAllRoomsByHomestay(Long homestayId) {
        // 1. Lấy danh sách xác phòng
        List<HomestayRoom> rooms = roomRepository.findAllByHomestayId(homestayId);
        if (rooms.isEmpty()) return List.of();

        List<Long> roomIds = rooms.stream().map(HomestayRoom::getId).toList();

        // 2. Kéo toàn bộ dữ liệu liên quan về (Dùng In-clause để tối ưu SQL)
        // Lấy Highlights kèm thông tin Amenity
        Map<Long, List<AmenityHighlightResponse>> highlightMap = getHighlightMap(roomIds);

        // Lấy RatePlans
        List<RoomRatePlan> allPlans = ratePlanRepository.findAllByRoomIdIn(roomIds);
        List<Long> planIds = allPlans.stream().map(RoomRatePlan::getId).toList();

        // Lấy Benefits cho các RatePlans
        Map<Long, List<String>> benefitMap = getBenefitMap(planIds);

        // 3. Map sang DTO và lắp ghép
        return rooms.stream().map(room -> {
            // Lọc các plans thuộc về phòng này
            List<RatePlanResponse> ratePlanResponses = allPlans.stream()
                    .filter(p -> p.getRoomId().equals(room.getId()))
                    .map(p -> RatePlanResponse.builder()
                            .id(p.getId())
                            .name(p.getName())
                            .price(p.getPrice())
                            .isNonRefundable(p.getIsNonRefundable())
                            .benefits(benefitMap.getOrDefault(p.getId(), List.of()))
                            .build())
                    .toList();

            return RoomResponse.builder()
                    .id(room.getId())
                    .name(room.getName())
                    .description(room.getDescription())
                    .tag(room.getTag())
                    .area(room.getArea())
                    .floor(room.getFloor())
                    .wing(room.getWing())
                    .checkInTime(room.getCheckInTime())
                    .maxGuests(room.getMaxGuests())
                    .bedCount(room.getBedCount())
                    .quantity(room.getQuantity())
                    .imageUrl(room.getImageUrl())
                    .highlights(highlightMap.getOrDefault(room.getId(), List.of()))
                    .ratePlans(ratePlanResponses)
                    .build();
        }).toList();
    }

    @Override
    public List<RoomResponse> findAvailableRooms(Long homestayId, LocalDate checkIn, LocalDate checkOut, int guests) {
        return List.of();
    }

    private Map<Long, List<AmenityHighlightResponse>> getHighlightMap(List<Long> roomIds) {
        if (roomIds.isEmpty()) return Map.of();

        // 1. Lấy dữ liệu đã JOIN sẵn từ DB (có kèm roomId)
        List<AmenityHighlightResponse> allHighlights =
                highlightRepository.findAllHighlightsByRoomIds(roomIds);

        // 2. Bây giờ đã có roomId để grouping
        return allHighlights.stream()
                .collect(Collectors.groupingBy(
                        AmenityHighlightResponse::getRoomId,
                        Collectors.toList()
                ));
    }

    // Helper: Lấy map Benefits (PlanId -> List<BenefitName>)
    private Map<Long, List<String>> getBenefitMap(List<Long> planIds) {
        if (planIds.isEmpty()) return Map.of();
        return benefitMappingRepository.findAllByRatePlanIdIn(planIds).stream()
                .collect(Collectors.groupingBy(RatePlanBenefitMapping::getRatePlanId,
                        Collectors.mapping(m -> {
                            var amenity = amenityRepository.findById(m.getAmenityId().longValue()).orElse(null);
                            return amenity != null ? amenity.getName() : "";
                        }, Collectors.toList())));
    }
}