package clyvasync.Clyvasync.service.homestay;

import clyvasync.Clyvasync.dto.response.PageResponse;
import clyvasync.Clyvasync.dto.response.ReviewResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface ReviewService {
    List<ReviewResponse> getReviewsByHomestay(Long homestayId);
    PageResponse<ReviewResponse> getReviewsByHomestay(Long homestayId, Pageable pageable);
}
