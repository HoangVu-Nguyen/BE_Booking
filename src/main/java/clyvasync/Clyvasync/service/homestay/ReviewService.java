package clyvasync.Clyvasync.service.homestay;

import clyvasync.Clyvasync.dto.response.PageResponse;
import clyvasync.Clyvasync.dto.response.ReviewResponse;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

public interface ReviewService {
    List<ReviewResponse> getReviewsByHomestay(Long homestayId);
    PageResponse<ReviewResponse> getReviewsByHomestay(Long homestayId, Pageable pageable);
    List<ReviewResponse> getReviewsByHomestayId(Long homestayId);
    Double getAverageRatingByHomestaysUpToDate(
            List<Long> homestayIds,
            OffsetDateTime targetDate
    );
    clyvasync.Clyvasync.dto.response.ReviewResponse createReview(Long userId, clyvasync.Clyvasync.dto.request.ReviewCreateRequest request);
    boolean checkReviewEligibility(Long userId, String bookingCode);
}
