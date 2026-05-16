package clyvasync.Clyvasync.controller.trip;


import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.TripResponse;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.trip.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/trips")
@RequiredArgsConstructor
public class TripController {
    private final TripService tripService;
    @GetMapping("/my-trips")
    public ApiResponse<List<TripResponse>> getMyTrips(@CurrentUserId Long currentUserId) {
        log.info("Client đang yêu cầu lấy danh sách My Trips cho user: {}", currentUserId);
        return ApiResponse.success(tripService.getUserTrips(currentUserId));
    }
}