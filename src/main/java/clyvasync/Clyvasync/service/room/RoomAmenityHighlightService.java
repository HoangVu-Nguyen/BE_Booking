package clyvasync.Clyvasync.service.room;

import clyvasync.Clyvasync.dto.response.AmenityHighlightResponse;

import java.util.List;
import java.util.Map;

public interface RoomAmenityHighlightService {
    Map<Long, List<AmenityHighlightResponse>> getHighlightsForRooms(List<Long> roomIds);
}
