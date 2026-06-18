package clyvasync.Clyvasync.controller.homestay;

import clyvasync.Clyvasync.dto.request.UpdateHomestayAmenitiesRequest;
import clyvasync.Clyvasync.dto.request.UpdateRoomAmenityHighlightsRequest;
import clyvasync.Clyvasync.dto.response.AmenityResponse;
import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.RoomAmenityHighlightResponse;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.homestay.AmenityService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/host/homestays")
@RequiredArgsConstructor
public class AmenityController {

    private final AmenityService amenityService;

    @GetMapping("/amenities")
    public ApiResponse<List<AmenityResponse>> getAllAmenities() {
        return ApiResponse.success(amenityService.getAllAmenities());
    }

    @GetMapping("/{homestayId}/amenities")
    public ApiResponse<List<Integer>> getHomestayAmenityIds(
            @PathVariable Long homestayId
    ) {
        return ApiResponse.success(
                amenityService.getHomestayAmenityIds(homestayId)
        );
    }

    @PutMapping("/{homestayId}/amenities")
    public ApiResponse<Void> updateHomestayAmenities(
            @CurrentUserId Long ownerId,
            @PathVariable Long homestayId,
            @RequestBody UpdateHomestayAmenitiesRequest request
    ) {
        amenityService.updateHomestayAmenities(ownerId, homestayId, request);
        return ApiResponse.success();
    }

    @GetMapping("/{homestayId}/rooms/{roomId}/amenity-highlights")
    public ApiResponse<List<RoomAmenityHighlightResponse>> getRoomAmenityHighlights(
            @PathVariable Long homestayId,
            @PathVariable Long roomId
    ) {
        return ApiResponse.success(
                amenityService.getRoomAmenityHighlights( homestayId, roomId)
        );
    }

    @PutMapping("/{homestayId}/rooms/{roomId}/amenity-highlights")
    public ApiResponse<Void> updateRoomAmenityHighlights(
            @CurrentUserId Long ownerId,
            @PathVariable Long homestayId,
            @PathVariable Long roomId,
            @RequestBody UpdateRoomAmenityHighlightsRequest request
    ) {
        amenityService.updateRoomAmenityHighlights(ownerId, homestayId, roomId, request);
        return ApiResponse.success();
    }
}