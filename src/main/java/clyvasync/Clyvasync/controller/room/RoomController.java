package clyvasync.Clyvasync.controller.room;


import clyvasync.Clyvasync.dto.request.BookingInitRequest;
import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.BookingDetailsResponse;
import clyvasync.Clyvasync.dto.response.BookingInitResponse;
import clyvasync.Clyvasync.dto.response.RoomDisplayResponse;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.booking.BookingService;
import clyvasync.Clyvasync.service.homestay.HomestayRoomService;
import clyvasync.Clyvasync.service.room.RoomCalendarService;
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


}
