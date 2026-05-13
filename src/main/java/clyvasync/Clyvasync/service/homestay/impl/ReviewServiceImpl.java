package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.dto.response.OwnerResponse;
import clyvasync.Clyvasync.dto.response.PageResponse;
import clyvasync.Clyvasync.dto.response.ReviewResponse;
import clyvasync.Clyvasync.mapper.homestay.ReviewMapper;
import clyvasync.Clyvasync.modules.homestay.entity.Review;
import clyvasync.Clyvasync.modules.homestay.entity.ReviewImage;
import clyvasync.Clyvasync.repository.homestay.ReviewImageRepository;
import clyvasync.Clyvasync.repository.homestay.ReviewRepository;
import clyvasync.Clyvasync.service.auth.UserService;
import clyvasync.Clyvasync.service.homestay.ReviewImageService;
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
    private final ReviewImageService reviewImageService;
    private final ReviewMapper reviewMapper;
    private final UserService userService;

    @Override
    public List<ReviewResponse> getReviewsByHomestay(Long homestayId) {
        return null;
    }

    @Override
    public PageResponse<ReviewResponse> getReviewsByHomestay(Long homestayId, Pageable pageable) {
        log.info("Lấy danh sách review phân trang cho homestay: {}", homestayId);

        Page<Review> reviewPage = reviewRepository.findAllByHomestayId(homestayId, pageable);
        List<Review> reviews = reviewPage.getContent();

        if (reviews.isEmpty()) {
            return PageResponse.<ReviewResponse>builder()
                    .content(List.of())
                    .totalElements(reviewPage.getTotalElements())
                    .totalPages(reviewPage.getTotalPages())
                    .build();
        }

        List<Long> reviewIds = reviews.stream().map(Review::getId).toList();
        List<Long> userIds = reviews.stream().map(Review::getGuestId).distinct().toList();

        Map<Long, List<String>> imagesMap = reviewImageService.getImagesForReviews(reviewIds);
        Map<Long, OwnerResponse> usersMap = userService.getOwnerInfos(userIds);

        List<ReviewResponse> content = reviews.stream().map(entity -> {
            ReviewResponse response = reviewMapper.toReviewResponse(entity);

            response.setImageUrls(imagesMap.getOrDefault(entity.getId(), List.of()));

            OwnerResponse userInfo = usersMap.get(entity.getGuestId());
            if (userInfo != null) {
                response.setFullName(userInfo.getFullName());
                response.setAvatarUrl(userInfo.getAvatar());
                response.setUserId(userInfo.getId());
            }

            return response;
        }).toList();

        return PageResponse.<ReviewResponse>builder()
                .content(content)
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .size(reviewPage.getSize())
                .number(reviewPage.getNumber())
                .build();
    }
    @Override
    public List<ReviewResponse> getReviewsByHomestayId(Long homestayId) {
        List<Review> reviews = reviewRepository.findAllByHomestayId(homestayId);
        if (reviews.isEmpty()) return List.of();

        List<Long> reviewIds = reviews.stream().map(Review::getId).toList();
        List<Long> userIds = reviews.stream().map(Review::getGuestId).distinct().toList();

        Map<Long, List<String>> imagesMap = reviewImageService.getImagesForReviews(reviewIds);
        Map<Long, OwnerResponse> usersMap = userService.getOwnerInfos(userIds);

        return reviews.stream().map(entity -> {
            ReviewResponse response = reviewMapper.toReviewResponse(entity);

            response.setImageUrls(imagesMap.getOrDefault(entity.getId(), List.of()));

            OwnerResponse userInfo = usersMap.get(entity.getGuestId());
            if (userInfo != null) {
                response.setFullName(userInfo.getFullName());
                response.setAvatarUrl(userInfo.getAvatar());
                response.setUserId(userInfo.getId());
            }

            return response;
        }).toList();
    }
}
