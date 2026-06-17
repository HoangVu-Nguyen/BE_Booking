package clyvasync.Clyvasync.controller.room;


import clyvasync.Clyvasync.dto.request.BatchUploadRequest;
import clyvasync.Clyvasync.dto.request.BookingInitRequest;
import clyvasync.Clyvasync.dto.request.MultiRoomBatchUploadRequest;
import clyvasync.Clyvasync.dto.request.RoomBatchUpdateRequest;
import clyvasync.Clyvasync.dto.response.*;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.booking.BookingService;
import clyvasync.Clyvasync.service.homestay.HomestayRoomService;
import clyvasync.Clyvasync.service.room.RoomCalendarService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController()
@RequestMapping("/api/v1/rooms")
@AllArgsConstructor
public class RoomController {
    private final RoomCalendarService roomCalendarService;
    private final HomestayRoomService homestayRoomService;

    @GetMapping("/homestays/{homestayId}/unavailable-dates")
    public ApiResponse<List<LocalDate>> getUnavailableDates(
            @PathVariable Long homestayId,
            @RequestParam int month,
            @RequestParam int year) {


        return ApiResponse.success(roomCalendarService.getUnavailableDates(homestayId, month, year));
    }
    @GetMapping("/homestays/{id}/rooms")
    public ApiResponse<List<RoomDisplayResponse>> getHomestayRooms(@PathVariable Long id) {
        return ApiResponse.success(homestayRoomService.getRoomsByHomestayId(id));
    }
    @PostMapping("/images/presign")
    public ApiResponse<List<PresignedUrlResponse>> prepareImageUploads(
            @CurrentUserId Long ownerId,
            @Valid @RequestBody MultiRoomBatchUploadRequest batchRequest) {

        List<PresignedUrlResponse> presignedUrls =
                homestayRoomService.prepareHomestayRoomImageBatch(ownerId, batchRequest);

        return ApiResponse.success(presignedUrls);
    }
    @PutMapping
    public ApiResponse<String> updateRooms(
            @CurrentUserId Long ownerId,
            @Valid @RequestBody RoomBatchUpdateRequest request) {
        System.out.println(request);

        homestayRoomService.updateRooms(ownerId, request);

        return ApiResponse.success();
    }

}
