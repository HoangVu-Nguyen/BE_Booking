package clyvasync.Clyvasync.controller.homestay;

import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.PortfolioTimelineResponse;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.homestay.HomestayService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/host")
@RequiredArgsConstructor
public class CalendarController {

    private final HomestayService homestayService;

    @GetMapping("/portfolio-timeline")
    public ApiResponse<PortfolioTimelineResponse> getOwnerPortfolioTimeline( @CurrentUserId Long currentUserId,
            @RequestParam int month,
            @RequestParam int year) {
        return ApiResponse.success(homestayService.getOwnerPortfolioTimeline(currentUserId, month, year));
    }


}