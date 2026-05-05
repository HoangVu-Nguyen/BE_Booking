package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.dto.response.ReviewResponse;
import clyvasync.Clyvasync.modules.homestay.entity.Review;
import clyvasync.Clyvasync.modules.homestay.entity.ReviewImage;
import clyvasync.Clyvasync.repository.homestay.ReviewImageRepository;
import clyvasync.Clyvasync.repository.homestay.ReviewRepository;
import clyvasync.Clyvasync.service.homestay.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class ReviewServiceImpl implements ReviewService {
    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;

    @Override
    public List<ReviewResponse> getReviewsByHomestay(Long homestayId) {
        List<Map<String, Object>> rawData = reviewRepository.findReviewsWithUserInfo(homestayId);

        if (rawData.isEmpty()) return Collections.emptyList();

        List<Long> reviewIds = rawData.stream()
                .map(m -> ((Number) m.get("id")).longValue())
                .toList();

        List<ReviewImage> allImages = reviewImageRepository.findAllByReviewIdIn(reviewIds);
        Map<Long, List<String>> imagesMap = allImages.stream()
                .collect(Collectors.groupingBy(
                        ReviewImage::getReviewId,
                        Collectors.mapping(ReviewImage::getImageUrl, Collectors.toList())
                ));

        return rawData.stream().map(m -> {
            Long rId = ((Number) m.get("id")).longValue();

            // FIX TẠI ĐÂY: Xử lý Instant từ Hibernate 6
            Object createdAtObj = m.get("created_at");
            java.time.LocalDateTime createdAt;

            if (createdAtObj instanceof java.time.Instant) {
                createdAt = java.time.LocalDateTime.ofInstant((java.time.Instant) createdAtObj, java.time.ZoneId.systemDefault());
            } else if (createdAtObj instanceof java.sql.Timestamp) {
                createdAt = ((java.sql.Timestamp) createdAtObj).toLocalDateTime();
            } else {
                // Fallback hoặc gán giá trị mặc định nếu cần
                createdAt = java.time.LocalDateTime.now();
            }

            return ReviewResponse.builder()
                    .id(rId)
                    .rating((Integer) m.get("rating"))
                    .comment((String) m.get("comment"))
                    .createdAt(createdAt) // Sử dụng biến createdAt đã xử lý ở trên
                    .userId(((Number) m.get("user_id")).longValue())
                    .fullName((String) m.get("fullName"))
                    .avatarUrl((String) m.get("avatarUrl"))
                    .guestPhotos(imagesMap.getOrDefault(rId, Collections.emptyList()))
                    .build();
        }).toList();
    }
}
