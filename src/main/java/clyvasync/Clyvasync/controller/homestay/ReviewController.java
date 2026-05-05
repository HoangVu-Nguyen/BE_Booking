package clyvasync.Clyvasync.controller.homestay;


import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.dto.response.PageResponse;
import clyvasync.Clyvasync.dto.response.ReviewResponse;
import clyvasync.Clyvasync.service.homestay.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * Lấy danh sách đánh giá phân trang cho một Homestay cụ thể.
     *
     * @param homestayId ID của homestay cần lấy review
     * @param pageable Tham số phân trang (mặc định page=0, size=4, sắp xếp theo ngày tạo mới nhất)
     * @return PageResponse chứa danh sách Review và metadata cho UI "Load more"
     */
    @GetMapping("/homestay/{homestayId}")
    public ApiResponse<PageResponse<ReviewResponse>> getHomestayReviews(
            @PathVariable Long homestayId,
            @PageableDefault(
                    size = 4,
                    sort = "created_at",
                    direction = Sort.Direction.DESC
            ) Pageable pageable) {

        PageResponse<ReviewResponse> result = reviewService.getReviewsByHomestay(homestayId, pageable);
        return ApiResponse.success(result);
    }
}