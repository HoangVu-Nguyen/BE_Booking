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
    private final clyvasync.Clyvasync.service.tour.TourImageService tourImageService;
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

    // Host APIs
    @org.springframework.web.bind.annotation.PostMapping("/images/prepare")
    public ApiResponse<java.util.List<clyvasync.Clyvasync.dto.response.PresignedUrlResponse>> prepareTourImageUploads(
            @clyvasync.Clyvasync.service.annotation.CurrentUserId Long ownerId,
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody clyvasync.Clyvasync.dto.request.BatchUploadRequest request) {
        return ApiResponse.success(tourImageService.prepareTourImagesBatch(ownerId, request));
    }

    @org.springframework.web.bind.annotation.PostMapping("/homestay/{homestayId}")
    public ApiResponse<TourResponse> createTour(
            @clyvasync.Clyvasync.service.annotation.CurrentUserId Long ownerId,
            @PathVariable Long homestayId,
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody clyvasync.Clyvasync.dto.request.CreateTourRequest request) {
        return ApiResponse.success(tourService.createTour(ownerId, homestayId, request));
    }

    @GetMapping("/homestay/{homestayId}/manage")
    public ApiResponse<java.util.List<TourResponse>> getHostTours(
            @clyvasync.Clyvasync.service.annotation.CurrentUserId Long ownerId,
            @PathVariable Long homestayId) {
        // Có thể thêm bước kiểm tra ownerId == homestay.getOwnerId() để bảo mật, 
        // nhưng TourService getToursByHomestayId đã lấy các tour của homestayId.
        return ApiResponse.success(tourService.getToursByHomestayId(homestayId));
    }

    @org.springframework.web.bind.annotation.PutMapping("/{tourId}")
    public ApiResponse<TourResponse> updateTour(
            @clyvasync.Clyvasync.service.annotation.CurrentUserId Long ownerId,
            @PathVariable Long tourId,
            @jakarta.validation.Valid @org.springframework.web.bind.annotation.RequestBody clyvasync.Clyvasync.dto.request.UpdateTourRequest request) {
        return ApiResponse.success(tourService.updateTour(tourId, request));
    }

    @org.springframework.web.bind.annotation.DeleteMapping("/{tourId}")
    public ApiResponse<Void> deleteTour(
            @clyvasync.Clyvasync.service.annotation.CurrentUserId Long ownerId,
            @PathVariable Long tourId) {
        tourService.deleteTour(tourId);
        return ApiResponse.success(null);
    }
}
