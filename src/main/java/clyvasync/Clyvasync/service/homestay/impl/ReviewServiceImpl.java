package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.dto.response.PageResponse;
import clyvasync.Clyvasync.dto.response.ReviewResponse;
import clyvasync.Clyvasync.modules.homestay.entity.Review;
import clyvasync.Clyvasync.modules.homestay.entity.ReviewImage;
import clyvasync.Clyvasync.repository.homestay.ReviewImageRepository;
import clyvasync.Clyvasync.repository.homestay.ReviewRepository;
import clyvasync.Clyvasync.service.homestay.ReviewService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
@Slf4j
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

    @Override
    @Transactional(readOnly = true)
    public PageResponse<ReviewResponse> getReviewsByHomestay(Long homestayId, Pageable pageable) {
        log.info("Fetching reviews for homestay ID: {} with pageable: {}", homestayId, pageable);

        // 1. Lấy dữ liệu phân trang từ Repository (Native Query trả về Map)
        Page<Map<String, Object>> rawDataPage = reviewRepository.findReviewsWithUserInfo(homestayId, pageable);

        if (rawDataPage.isEmpty()) {
            return PageResponse.<ReviewResponse>builder()
                    .content(Collections.emptyList())
                    .totalElements(0)
                    .totalPages(0)
                    .last(true)
                    .build();
        }

        // 2. Thu thập tất cả Review IDs để lấy ảnh trong 1 lần gọi (Tránh N+1 Query)
        List<Long> reviewIds = rawDataPage.getContent().stream()
                .map(m -> ((Number) m.get("id")).longValue())
                .distinct()
                .toList();

        // 3. Lấy ảnh và nhóm theo Review ID
        Map<Long, List<String>> imagesMap = fetchImagesMap(reviewIds);

        // 4. Map dữ liệu sang DTO
        List<ReviewResponse> content = rawDataPage.getContent().stream()
                .map(m -> mapToReviewResponse(m, imagesMap))
                .toList();

        // 5. Tính toán Rating trung bình (Tùy chọn: Có thể lấy từ bảng Homestay để tối ưu hơn)
        double averageRating = content.stream()
                .mapToInt(ReviewResponse::getRating)
                .average()
                .orElse(0.0);

        return PageResponse.<ReviewResponse>builder()
                .content(content)
                .totalElements(rawDataPage.getTotalElements())
                .totalPages(rawDataPage.getTotalPages())
                .size(rawDataPage.getSize())
                .number(rawDataPage.getNumber())
                .last(rawDataPage.isLast())
                .averageRating(averageRating) // Thêm field này vào PageResponse nếu cần
                .build();
    }
    /**
     * Map dữ liệu từ Map SQL sang ReviewResponse DTO
     */
    private ReviewResponse mapToReviewResponse(Map<String, Object> m, Map<Long, List<String>> imagesMap) {
        Long rId = ((Number) m.get("id")).longValue();

        return ReviewResponse.builder()
                .id(rId)
                .rating(((Number) m.get("rating")).intValue())
                .comment((String) m.get("comment"))
                .createdAt(convertToLocalDateTime(m.get("created_at")))
                .userId(((Number) m.get("user_id")).longValue())
                .fullName((String) m.get("fullName"))
                .avatarUrl((String) m.get("avatarUrl"))
                .guestPhotos(imagesMap.getOrDefault(rId, Collections.emptyList()))
                .build();
    }

    /**
     * Xử lý convert Time đa dạng từ các phiên bản Hibernate/Database khác nhau
     */
    private LocalDateTime convertToLocalDateTime(Object obj) {
        if (obj == null) return LocalDateTime.now();

        if (obj instanceof Instant instant) {
            return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
        }
        if (obj instanceof java.sql.Timestamp timestamp) {
            return timestamp.toLocalDateTime();
        }
        if (obj instanceof LocalDateTime localDateTime) {
            return localDateTime;
        }

        log.warn("Unknown date type: {}. Returning current time.", obj.getClass().getName());
        return LocalDateTime.now();
    }

    /**
     * Lấy danh sách ảnh theo ID và Grouping
     */
    private Map<Long, List<String>> fetchImagesMap(List<Long> reviewIds) {
        try {
            return reviewImageRepository.findAllByReviewIdIn(reviewIds).stream()
                    .collect(Collectors.groupingBy(
                            ReviewImage::getReviewId,
                            Collectors.mapping(ReviewImage::getImageUrl, Collectors.toList())
                    ));
        } catch (Exception e) {
            log.error("Error fetching review images for IDs: {}", reviewIds, e);
            return Collections.emptyMap();
        }
    }
}
