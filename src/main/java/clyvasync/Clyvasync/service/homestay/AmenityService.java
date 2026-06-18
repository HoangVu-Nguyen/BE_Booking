package clyvasync.Clyvasync.service.homestay;

import clyvasync.Clyvasync.dto.request.UpdateHomestayAmenitiesRequest;
import clyvasync.Clyvasync.dto.request.UpdateRoomAmenityHighlightsRequest;
import clyvasync.Clyvasync.dto.response.AmenityHighlightResponse;
import clyvasync.Clyvasync.dto.response.AmenityResponse;
import clyvasync.Clyvasync.dto.response.RoomAmenityHighlightResponse;

import java.util.List;
import java.util.Map;

public interface AmenityService {
    List<AmenityResponse> getAmenitiesByHomestayId(Long homestayId);

    Map<Long, List<AmenityResponse>> getAmenitiesForHomestays(List<Long> homestayIds);
    List<AmenityResponse> getAllAmenities();

    List<Integer> getHomestayAmenityIds(Long homestayId);

    void updateHomestayAmenities(Long ownerId, Long homestayId, UpdateHomestayAmenitiesRequest request);

    List<AmenityHighlightResponse> getRoomAmenityHighlights(Long roomId);

    void updateRoomAmenityHighlights(Long ownerId, Long homestayId, Long roomId, UpdateRoomAmenityHighlightsRequest request);
    List<RoomAmenityHighlightResponse> getRoomAmenityHighlights(
            Long homestayId,
            Long roomId
    );

    void updateRoomAmenityHighlights(
            Long homestayId,
            Long roomId,
            UpdateRoomAmenityHighlightsRequest request
    );
}
