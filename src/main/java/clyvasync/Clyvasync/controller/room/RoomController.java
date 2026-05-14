package clyvasync.Clyvasync.controller.room;


import clyvasync.Clyvasync.dto.request.BookingInitRequest;
import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.BookingDetailsResponse;
import clyvasync.Clyvasync.dto.response.BookingInitResponse;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.booking.BookingService;
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

    @GetMapping("/homestays/{homestayId}/unavailable-dates")
    public ApiResponse<List<LocalDate>> getUnavailableDates(
            @PathVariable Long homestayId,
            @RequestParam int month,
            @RequestParam int year) {

        List<LocalDate> blockedDates = roomCalendarService.getUnavailableDates(homestayId, month, year);

        return ApiResponse.success(blockedDates);
    }


}
