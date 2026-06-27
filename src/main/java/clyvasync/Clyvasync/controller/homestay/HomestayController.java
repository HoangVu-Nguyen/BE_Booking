package clyvasync.Clyvasync.controller.homestay;


import clyvasync.Clyvasync.dto.request.BatchUploadRequest;
import clyvasync.Clyvasync.dto.request.HomestayBatchUploadRequest;
import clyvasync.Clyvasync.dto.request.HomestayRequest;
import clyvasync.Clyvasync.dto.request.HomestaySearchRequest;
import clyvasync.Clyvasync.dto.response.*;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.homestay.HomestayImageService;
import clyvasync.Clyvasync.service.homestay.HomestayRoomService;
import clyvasync.Clyvasync.service.homestay.HomestayService;
import clyvasync.Clyvasync.service.homestay.HomestayVerificationService;
import clyvasync.Clyvasync.service.tour.TourService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.checkerframework.checker.units.qual.C;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/homestays")
@RequiredArgsConstructor
public class HomestayController {

    private final HomestayService homestayService;
    private final HomestayRoomService homestayRoomService;
    private final TourService tourService;
    private final HomestayImageService homestayImageService;
    private final HomestayVerificationService verificationService;

    @GetMapping("/search")
    public ApiResponse<Page<HomestayResponse>> searchHomestays(
            HomestaySearchRequest filters,

            @PageableDefault(size = 10, sort = "averageRating", direction = Sort.Direction.DESC) Pageable pageable
    ) {

        Page<HomestayResponse> result = homestayService.searchHomestays(filters, pageable);

        return ApiResponse.success(result);
    }
    @GetMapping("/{id}")
    public ApiResponse<HomestayDetailResponse> getHomestayById(
            @CurrentUserId Long userId,
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam(required = false, defaultValue = "1") Integer guests
    ) {
        // Truyền thêm các tham số lọc vào service để lấy danh sách phòng chính xác
        return ApiResponse.success(homestayService.getHomestayDetail(userId, id, checkIn, checkOut, guests));
    }


    @GetMapping("/my-properties")
    public ApiResponse<List<HomestayResponse>> getMyHomestays(@CurrentUserId Long ownerId) {
        return ApiResponse.success(homestayService.getByOwnerId(ownerId));
    }


    @PostMapping
    public ApiResponse<HomestayResponse> createHomestay(
            @Valid @RequestBody HomestayRequest request,
            @CurrentUserId Long ownerId) {
        return ApiResponse.success(homestayService.createHomestay(request, ownerId));
    }


    @PutMapping("/{id}")
    public ApiResponse<HomestayResponse> updateHomestay(
            @PathVariable Long id,
            @Valid @RequestBody HomestayRequest request,
            @CurrentUserId Long ownerId) {
        System.out.println(request);
        return ApiResponse.success(homestayService.updateHomestay(id, request, ownerId));
    }


    @DeleteMapping("/{id}")
    public ApiResponse<Void> deleteHomestay(
            @PathVariable Long id,
            @CurrentUserId Long ownerId) {
        homestayService.deleteHomestay(id, ownerId);
        return ApiResponse.success(null);
    }


    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(
            @PathVariable Long id,
            @RequestParam String status,
            @CurrentUserId Long ownerId) {
        homestayService.updateStatus(id, status, ownerId);
        return ApiResponse.success(null);
    }
    @GetMapping("/{id}/available-rooms")
    public ApiResponse<BookingAvailabilityResponse> getHomestayAvailability( // Đổi kiểu trả về
                                                                             @PathVariable Long id,
                                                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
                                                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
                                                                             @RequestParam(defaultValue = "1") Integer guests
    ) {
        List<RoomResponse> availableRooms = homestayRoomService.findAvailableRooms(id, checkIn, checkOut, guests);

        List<TourResponse> suggestedTours = tourService.getAvailableToursForBookingDates(id, checkIn, checkOut);

        BookingAvailabilityResponse responseData = BookingAvailabilityResponse.builder()
                .rooms(availableRooms)
                .suggestedTours(suggestedTours)
                .build();

        return ApiResponse.success(responseData);
    }
    @PostMapping("/draft")
    public ApiResponse<HomestayResponse> createDraft(
            @RequestBody HomestayRequest request,
            @CurrentUserId Long ownerId) {
        System.out.println(request);

        return ApiResponse.success(
                homestayService.createHomestay(request, ownerId)
        );
    }
    // ========================================================
    // API CẤP PRESIGNED URL CHO BATCH UPLOAD ẢNH HOMESTAY
    // ========================================================
    @PostMapping("/images/presign")
    public ApiResponse<List<PresignedUrlResponse>> prepareImageUploads(
            @CurrentUserId Long ownerId,
            @Valid @RequestBody BatchUploadRequest batchRequest) {

        // Gọi hàm chuẩn bị lô ảnh PENDING mà ta đã định nghĩa trong HomestayService
        List<PresignedUrlResponse> presignedUrls = homestayImageService.prepareHomestayImagesBatch(ownerId, batchRequest);

        return ApiResponse.success(presignedUrls);
    }
    @GetMapping("/{homestayId}/rooms")
    public ApiResponse<List<RoomDisplayResponse>> getRoomsByHomestayId(
            @PathVariable Long homestayId
    ) {
        return ApiResponse.<List<RoomDisplayResponse>>builder()
                .data(homestayRoomService.getRoomsByHomestayId(homestayId))
                .build();
    }
    @PostMapping("/{homestayId}/documents/prepare")
    public ApiResponse<List<PreUploadResponse>> prepareUploads(
            @PathVariable Long homestayId,
            @Valid @RequestBody HomestayBatchUploadRequest request,
            @CurrentUserId Long userId){
        return ApiResponse.success(verificationService.prepareHomestayUploads(
                homestayId,
                userId,
                request
        ));
    }
    @PatchMapping("/{homestayId}/documents/{documentId}/confirm")
    public ApiResponse<?> confirmUpload(
            @PathVariable Long homestayId,
            @PathVariable Long documentId,
          @CurrentUserId Long userId) {
        verificationService.confirmDocumentUpload(homestayId, documentId, userId);

        return ApiResponse.success();
    }
    @GetMapping("/{homestayId}/documents")
    public ApiResponse<?> getDocuments(
            @PathVariable Long homestayId,
            @CurrentUserId Long userId) {
        return ApiResponse.success(verificationService.getDocuments(
                homestayId,
                userId
        ));
    }
}