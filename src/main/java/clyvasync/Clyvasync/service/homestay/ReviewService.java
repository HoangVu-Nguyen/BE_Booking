package clyvasync.Clyvasync.service.homestay;

import clyvasync.Clyvasync.dto.response.ReviewResponse;

import java.util.List;

public interface ReviewService {
    List<ReviewResponse> getReviewsByHomestay(Long homestayId);
}
