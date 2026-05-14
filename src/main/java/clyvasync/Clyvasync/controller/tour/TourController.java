package clyvasync.Clyvasync.controller.tour;

import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.TourDetailResponse;
import clyvasync.Clyvasync.dto.response.TourResponse;
import clyvasync.Clyvasync.service.tour.TourService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tours")
@RequiredArgsConstructor
public class TourController {
    private final TourService tourService;
    // Lấy toàn bộ tour (Trang khám phá)
    @GetMapping
    public ApiResponse<Page<TourResponse>> getAllTours(
            @PageableDefault(size = 12, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ApiResponse.success(tourService.getAllTours(pageable));
    }

    @GetMapping("/{id}")
    public ApiResponse<TourDetailResponse> getTourById(@PathVariable Long id) {
        return ApiResponse.success(tourService.getTourById(id));
    }

}
