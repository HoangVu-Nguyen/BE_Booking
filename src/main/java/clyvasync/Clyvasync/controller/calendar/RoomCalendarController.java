package clyvasync.Clyvasync.controller.calendar;

import clyvasync.Clyvasync.dto.request.BatchUpdateCalendarRequest;
import clyvasync.Clyvasync.dto.response.*;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.homestay.HomestayService;
import clyvasync.Clyvasync.service.room.RoomCalendarService;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.C;
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
    private final HomestayService homestayService;

    /**
     * API Lấy dữ liệu Lưới lịch (Tape Chart) cho Frontend
     * GET /api/v1/host/homestays/{homestayId}/calendar?startDate=2026-05-01&endDate=2026-05-31
     */
    @GetMapping("/{homestayId}/calendar")
    public ApiResponse<HomestayCalendarResponse> getHomestayCalendar(@CurrentUserId Long ownerId,
            @PathVariable Long homestayId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Ngày bắt đầu không được lớn hơn ngày kết thúc");
        }
        return ApiResponse.success(roomCalendarService.getHomestayCalendar(ownerId,homestayId, startDate, endDate));
    }
    @GetMapping("/portfolio-timeline")
    public ApiResponse<PortfolioTimelineResponse> getOwnerPortfolioTimeline(@CurrentUserId Long currentUserId,
                                                                            @RequestParam int month,
                                                                            @RequestParam int year) {
        return ApiResponse.success(homestayService.getOwnerPortfolioTimeline(currentUserId, month, year));
    }
    @PostMapping("/calendar/batch-update")
    public ApiResponse<Void> batchUpdate(@RequestBody BatchUpdateCalendarRequest request) {
        roomCalendarService.batchUpdateCalendar(request);
        return ApiResponse.success();
    }
    @GetMapping("/{homestayId}/calendar/details")
    public ApiResponse<List<CalendarInventoryResponse>> getCalendarDetails(
            @PathVariable Long homestayId,
            @RequestParam Long roomId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return ApiResponse.success(roomCalendarService.getCalendarDetails(roomId, startDate, endDate));
    }


}