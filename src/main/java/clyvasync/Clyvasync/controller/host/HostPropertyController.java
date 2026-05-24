package clyvasync.Clyvasync.controller.host;

import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.HostPortfolioSummaryResponse;
import clyvasync.Clyvasync.dto.response.PropertySummaryResponse;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.homestay.HomestayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/host/properties")
@RequiredArgsConstructor
public class HostPropertyController {

    private final HomestayService homestayService;

    @GetMapping
    public ApiResponse<List<PropertySummaryResponse>> getMyProperties(@CurrentUserId Long currentUserId) {

        return ApiResponse.success(homestayService.getHostProperties(currentUserId));
    }
    @GetMapping("/summary")
    public ApiResponse<HostPortfolioSummaryResponse> getPortfolioSummary(@CurrentUserId Long currentUserId) {
       return  ApiResponse.success(homestayService.getPortfolioSummary(currentUserId));
    }
}