package clyvasync.Clyvasync.service.homestay.impl;

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
        List<HomestayRoom> rooms = roomRepository.findAllByHomestayIdAndStatus(homestayId, RoomStatus.ACTIVE);
        return processRoomResponses(rooms);
    }

    @Override
    public List<RoomResponse> findAvailableRooms(Long homestayId, LocalDate checkIn, LocalDate checkOut, int guests) {
        // Tìm phòng trống theo thời gian và số khách
        List<HomestayRoom> availableRooms = roomRepository.findAvailableRooms(homestayId, checkIn, checkOut, guests);
        return processRoomResponses(availableRooms);
    }

    /**
     * Hàm dùng chung để lắp ghép dữ liệu Highlights, RatePlans và Benefits
     * Giúp tránh lặp code và đảm bảo tính nhất quán dữ liệu
     */
    private List<RoomResponse> processRoomResponses(List<HomestayRoom> rooms) {
        if (rooms.isEmpty()) return List.of();

        List<Long> roomIds = rooms.stream().map(HomestayRoom::getId).toList();

        // 1. Lấy dữ liệu bổ trợ (Batch fetching để tránh N+1)
        List<RoomRatePlan> ratePlans = roomRatePlanService.getAllRoomRatePlans(roomIds);
        List<Long> ratePlanIds = ratePlans.stream().map(RoomRatePlan::getId).toList();

        Map<Long, List<AmenityHighlightResponse>> highlightsMap =
                roomAmenityHighlightService.getHighlightsForRooms(roomIds);

        Map<Long, List<String>> benefitsMap =
                ratePlanBenefitMappingService.findBenefitsByPlanIds(ratePlanIds);

        // 2. Map RatePlans sang DTO kèm theo Benefits (tích xanh)
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

        // 3. Lắp ghép vào RoomResponse cuối cùng
        return rooms.stream().map(room -> {
            RoomResponse response = roomMapper.toRoomResponse(room);
            response.setHighlights(highlightsMap.getOrDefault(room.getId(), List.of()));
            response.setRatePlans(ratePlanResponseMap.getOrDefault(room.getId(), List.of()));
            return response;
        }).toList();
    }

    @Override
    public List<HomestayRoomSummary> getRoomSummaries(List<Long> homestayIds) {
        return roomRepository.getRoomSummaries(homestayIds);
    }
}