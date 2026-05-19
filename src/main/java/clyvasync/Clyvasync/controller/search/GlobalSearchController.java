package clyvasync.Clyvasync.controller.search;


import clyvasync.Clyvasync.dto.request.GlobalSearchRequest;
import clyvasync.Clyvasync.dto.response.GlobalSearchResponse;
import clyvasync.Clyvasync.dto.response.ApiResponse; // (Hoặc wrapper của bác)
import clyvasync.Clyvasync.service.homestay.HomestayService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class GlobalSearchController {

    private final HomestayService homestayService;


    @GetMapping("/cinematic")
    public ApiResponse<List<GlobalSearchResponse>> executeCinematicSearch(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) Integer guests,
            @RequestParam(required = false) Integer bedrooms,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) List<Integer> amenityIds
    ) {
        // TẠO OBJECT VỚI TÊN TRƯỜNG TƯỜNG MINH
        GlobalSearchRequest request = new GlobalSearchRequest(
                keyword,
                category,
                minPrice, // Đảm bảo khớp thứ tự với Record
                maxPrice,
                guests,
                bedrooms,
                minRating,
                amenityIds
        );
        System.out.println(request);

        return ApiResponse.success(homestayService.cinematicSearch(request));
    }
}