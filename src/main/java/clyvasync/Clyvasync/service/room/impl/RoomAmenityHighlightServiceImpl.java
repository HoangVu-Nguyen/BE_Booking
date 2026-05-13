package clyvasync.Clyvasync.service.room.impl;

import clyvasync.Clyvasync.dto.response.AmenityHighlightResponse;
import clyvasync.Clyvasync.modules.room.RoomRatePlan;
import clyvasync.Clyvasync.repository.room.RoomAmenityHighlightRepository;
import clyvasync.Clyvasync.service.room.RoomAmenityHighlightService;
import clyvasync.Clyvasync.service.room.RoomRatePlanService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class RoomAmenityHighlightServiceImpl implements RoomAmenityHighlightService {
    private final RoomAmenityHighlightRepository roomAmenityHighlightRepository;

    @Override
    public Map<Long, List<AmenityHighlightResponse>> getHighlightsForRooms(List<Long> roomIds) {
        if (roomIds.isEmpty()) return Map.of();

        List<AmenityHighlightResponse> allHighlights =
                roomAmenityHighlightRepository.findAllHighlightsByRoomIds(roomIds);

        return allHighlights.stream()
                .collect(Collectors.groupingBy(
                        AmenityHighlightResponse::getRoomId,
                        Collectors.toList()
                ));
    }
}

