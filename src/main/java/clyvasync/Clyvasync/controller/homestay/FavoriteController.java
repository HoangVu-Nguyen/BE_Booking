package clyvasync.Clyvasync.controller.homestay;


import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.HomestayCardResponse;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;
import clyvasync.Clyvasync.service.homestay.FavoriteService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/favorites")
@RequiredArgsConstructor
public class FavoriteController {

    private final FavoriteService favoriteService;

    /**
     * Endpoint to toggle a homestay in the user's curated collection.
     * POST /api/v1/favorites/homestays/{homestayId}/toggle
     */
    @PostMapping("/homestays/{homestayId}/toggle")
    public ApiResponse<Boolean> toggleHomestayFavorite(@PathVariable Long homestayId, @CurrentUserId Long currentUserId) {
        return ApiResponse.success(favoriteService.toggleFavorite(currentUserId, homestayId));
    }
    @GetMapping("/my-collection")
    public ApiResponse<List<HomestayCardResponse>> getMyCuratedCollection(@CurrentUserId Long currentUserId) {
        return ApiResponse.success(favoriteService.getUserFavoriteHomestays(currentUserId));
    }
}