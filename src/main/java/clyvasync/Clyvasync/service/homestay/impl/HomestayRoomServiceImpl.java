package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.dto.response.AmenityHighlightResponse;
import clyvasync.Clyvasync.dto.response.RoomResponse;
import clyvasync.Clyvasync.dto.summary.HomestayRoomSummary;
import clyvasync.Clyvasync.mapper.room.RoomMapper;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayRoom;
import clyvasync.Clyvasync.modules.room.RatePlanBenefitMapping;
import clyvasync.Clyvasync.modules.room.RoomRatePlan;
import clyvasync.Clyvasync.repository.homestay.*;
import clyvasync.Clyvasync.repository.room.RatePlanBenefitMappingRepository;
import clyvasync.Clyvasync.repository.room.RoomAmenityHighlightRepository;
import clyvasync.Clyvasync.repository.room.RoomRatePlanRepository;
import clyvasync.Clyvasync.service.homestay.HomestayRoomService;
import clyvasync.Clyvasync.service.room.RoomRatePlanService;
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
    private final RoomRatePlanService roomRatePlanService;
    private final RatePlanBenefitMappingRepository benefitMappingRepository;
    private final RoomMapper roomMapper;

    @Override
    public List<RoomResponse> getAllRoomsByHomestay(Long homestayId) {
        // 1. Lấy danh sách xác phòng
//        List<HomestayRoom> rooms = roomRepository.findAllByHomestayId(homestayId);
//        if (rooms.isEmpty()) return List.of();
//
//        List<Long> roomIds = rooms.stream().map(HomestayRoom::getId).toList();
//
//        // 2. Kéo toàn bộ dữ liệu liên quan về (Dùng In-clause để tối ưu SQL)
//        // Lấy Highlights kèm thông tin Amenity
//        Map<Long, List<AmenityHighlightResponse>> highlightMap = getHighlightMap(roomIds);
//
//        // Lấy RatePlans
//        List<RoomRatePlan> allPlans = ratePlanRepository.findAllByRoomIdIn(roomIds);
//        List<Long> planIds = allPlans.stream().map(RoomRatePlan::getId).toList();
//
//        // Lấy Benefits cho các RatePlans
//        Map<Long, List<String>> benefitMap = getBenefitMap(planIds);
//
//        // 3. Map sang DTO và lắp ghép
//        return rooms.stream().map(room -> {
//            // Lọc các plans thuộc về phòng này
//            List<RatePlanResponse> ratePlanResponses = allPlans.stream()
//                    .filter(p -> p.getRoomId().equals(room.getId()))
//                    .map(p -> RatePlanResponse.builder()
//                            .id(p.getId())
//                            .name(p.getName())
//                            .price(p.getPrice())
//                            .isNonRefundable(p.getIsNonRefundable())
//                            .benefits(benefitMap.getOrDefault(p.getId(), List.of()))
//                            .build())
//                    .toList();
//
//            return RoomResponse.builder()
//                    .id(room.getId())
//                    .name(room.getName())
//                    .description(room.getDescription())
//                    .tag(room.getTag())
//                    .area(room.getArea())
//                    .floor(room.getFloor())
//                    .wing(room.getWing())
//                    .checkInTime(room.getCheckInTime())
//                    .maxGuests(room.getMaxGuests())
//                    .bedCount(room.getBedCount())
//                    .quantity(room.getQuantity())
//                    .imageUrl(room.getImageUrl())
//                    .highlights(highlightMap.getOrDefault(room.getId(), List.of()))
//                    .ratePlans(ratePlanResponses)
//                    .build();
//        }).toList();
        return List.of();
    }

    @Override
    public List<RoomResponse> findAvailableRooms(Long homestayId, LocalDate checkIn, LocalDate checkOut, int guests) {
        // 1. Tìm các phòng còn trống thỏa mãn điều kiện
        List<HomestayRoom> availableRooms = roomRepository.findAvailableRooms(homestayId, checkIn, checkOut, guests);

        if (availableRooms.isEmpty()) {
            return List.of();
        }

        List<Long> roomIds = availableRooms.stream().map(HomestayRoom::getId).toList();

        // 2. Lấy tất cả gói giá của các phòng trống này (Mapping mềm)
        List<RoomRatePlan> ratePlans = roomRatePlanService.getAllRoomRatePlans(roomIds);

        // Group gói giá theo roomId để dễ map
        Map<Long, List<RoomRatePlan>> ratePlanMap = ratePlans.stream()
                .collect(Collectors.groupingBy(RoomRatePlan::getRoomId));

        // 3. Map sang DTO để trả về cho FE
        return availableRooms.stream().map(room -> {
            RoomResponse response = roomMapper.toRoomResponse(room);
            // Gán danh sách gói giá vào từng phòng
            response.setRatePlans(ratePlanMap.getOrDefault(room.getId(), List.of()));
            return response;
        }).toList();
    }

    @Override
    public List<HomestayRoomSummary> getRoomSummaries(List<Long> homestayIds) {
        return roomRepository.getRoomSummaries(homestayIds);
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