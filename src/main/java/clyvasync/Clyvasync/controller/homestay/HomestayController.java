package clyvasync.Clyvasync.controller.homestay;


import clyvasync.Clyvasync.dto.request.HomestayRequest;
import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.HomestayResponse;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.homestay.HomestayService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/homestays")
@RequiredArgsConstructor
public class HomestayController {

    private final HomestayService homestayService;

    @GetMapping("/search")
    public ApiResponse<Page<HomestayResponse>> searchHomestays(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer guests,
            @PageableDefault(size = 10) Pageable pageable) {

        Page<HomestayResponse> result = homestayService.searchHomestays(city, minPrice, maxPrice, guests, pageable);
        return ApiResponse.success(result);
    }


    @GetMapping("/{id}")
    public ApiResponse<HomestayResponse> getHomestayById(@PathVariable Long id) {
        return ApiResponse.success(homestayService.getById(id));
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
}