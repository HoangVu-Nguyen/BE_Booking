package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.dto.projection.RoomAvailabilityProjection;
import clyvasync.Clyvasync.dto.response.AmenityHighlightResponse;
import clyvasync.Clyvasync.dto.response.RatePlanResponse;
import clyvasync.Clyvasync.dto.response.RoomResponse;
import clyvasync.Clyvasync.dto.summary.HomestayRoomSummary;
import clyvasync.Clyvasync.enums.room.RoomStatus;
import clyvasync.Clyvasync.mapper.room.RoomMapper;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayRoom;
import clyvasync.Clyvasync.modules.room.RatePlanBenefitMapping;
import clyvasync.Clyvasync.modules.room.RoomRatePlan;
import clyvasync.Clyvasync.repository.homestay.*;
import clyvasync.Clyvasync.repository.room.RatePlanBenefitMappingRepository;
import clyvasync.Clyvasync.repository.room.RoomAmenityHighlightRepository;
import clyvasync.Clyvasync.repository.room.RoomRatePlanRepository;
import clyvasync.Clyvasync.service.homestay.HomestayRoomService;
import clyvasync.Clyvasync.service.room.RatePlanBenefitMappingService;
import clyvasync.Clyvasync.service.room.RoomAmenityHighlightService;
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
    private final RoomRatePlanService roomRatePlanService;
    private final RoomMapper roomMapper;
    private final RoomAmenityHighlightService roomAmenityHighlightService;
    private final RatePlanBenefitMappingService ratePlanBenefitMappingService;

    @Override
    public List<RoomResponse> getAllRoomsByHomestay(Long homestayId) {
        // Khi xem tất cả phòng (không chọn ngày), availableQuantity chính là quantity gốc
        List<HomestayRoom> rooms = roomRepository.findAllByHomestayIdAndStatus(homestayId, RoomStatus.ACTIVE);

        Map<Long, Integer> availableCountMap = rooms.stream()
                .collect(Collectors.toMap(HomestayRoom::getId, HomestayRoom::getQuantity));

        return processRoomResponses(rooms, availableCountMap);
    }

    @Override
    public List<RoomResponse> findAvailableRooms(Long homestayId, LocalDate checkIn, LocalDate checkOut, int guests) {
        // 1. Lấy danh sách Projection (chỉ chứa ID và số lượng trống)
        List<RoomAvailabilityProjection> projections = roomRepository.findAvailableRoomsProjections(homestayId, checkIn, checkOut, guests);

        if (projections.isEmpty()) return List.of();

        // 2. Tạo Map số lượng trống từ Projection (Trực tiếp lấy getId() sẽ không bao giờ Null)
        Map<Long, Integer> availableCountMap = projections.stream()
                .collect(Collectors.toMap(
                        RoomAvailabilityProjection::getId,
                        RoomAvailabilityProjection::getAvailableQty
                ));

        // 3. Trích xuất danh sách ID
        List<Long> roomIds = projections.stream()
                .map(RoomAvailabilityProjection::getId)
                .toList();

        // 4. Lấy danh sách Entity Room bằng findAllById (Chỉ tốn đúng 1 câu lệnh SELECT ... WHERE id IN (...))
        List<HomestayRoom> rooms = roomRepository.findAllById(roomIds);

        // 5. Đưa vào hàm xử lý chung của bác
        return processRoomResponses(rooms, availableCountMap);
    }
    /**
     * Hàm dùng chung để lắp ghép dữ liệu bổ trợ và gán availableQuantity
     */
    private List<RoomResponse> processRoomResponses(List<HomestayRoom> rooms, Map<Long, Integer> availableCountMap) {
        if (rooms.isEmpty()) return List.of();

        List<Long> roomIds = rooms.stream().map(HomestayRoom::getId).toList();

        // Batch fetching dữ liệu liên quan (Mapping mềm)
        List<RoomRatePlan> ratePlans = roomRatePlanService.getAllRoomRatePlans(roomIds);
        List<Long> ratePlanIds = ratePlans.stream().map(RoomRatePlan::getId).toList();

        Map<Long, List<AmenityHighlightResponse>> highlightsMap =
                roomAmenityHighlightService.getHighlightsForRooms(roomIds);

        Map<Long, List<String>> benefitsMap =
                ratePlanBenefitMappingService.findBenefitsByPlanIds(ratePlanIds);

        // Map RatePlans sang DTO
        Map<Long, List<RatePlanResponse>> ratePlanResponseMap = ratePlans.stream()
                .collect(Collectors.groupingBy(
                        RoomRatePlan::getRoomId,
                        Collectors.mapping(plan -> RatePlanResponse.builder()
                                        .id(plan.getId())
                                        .name(plan.getName())
                                        .price(plan.getPrice())
                                        .isNonRefundable(plan.getIsNonRefundable())
                                        .benefits(benefitsMap.getOrDefault(plan.getId(), List.of()))
                                        .build(),
                                Collectors.toList())
                ));

        // Build Response cuối cùng
        return rooms.stream().map(room -> {
            RoomResponse response = roomMapper.toRoomResponse(room);
            response.setHighlights(highlightsMap.getOrDefault(room.getId(), List.of()));
            response.setRatePlans(ratePlanResponseMap.getOrDefault(room.getId(), List.of()));

            // Gán con số khả dụng thực tế
            response.setAvailableQuantity(availableCountMap.getOrDefault(room.getId(), 0));

            return response;
        }).toList();
    }

    @Override
    public List<HomestayRoomSummary> getRoomSummaries(List<Long> homestayIds) {
        return roomRepository.getRoomSummaries(homestayIds);
    }
}