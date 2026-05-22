package clyvasync.Clyvasync.controller.calendar;

import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.CalendarRoomResponse;
import clyvasync.Clyvasync.service.room.RoomCalendarService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/host/homestays")
@RequiredArgsConstructor
public class RoomCalendarController {

    private final RoomCalendarService roomCalendarService;

    /**
     * API Lấy dữ liệu Lưới lịch (Tape Chart) cho Frontend
     * GET /api/v1/host/homestays/{homestayId}/calendar?startDate=2026-05-01&endDate=2026-05-31
     */
    @GetMapping("/{homestayId}/calendar")
    public ApiResponse<List<CalendarRoomResponse>> getHomestayCalendar(
            @PathVariable Long homestayId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu không được lớn hơn ngày kết thúc");
        }
        return ApiResponse.success(roomCalendarService.getHomestayCalendar(homestayId, startDate, endDate));
    }
}