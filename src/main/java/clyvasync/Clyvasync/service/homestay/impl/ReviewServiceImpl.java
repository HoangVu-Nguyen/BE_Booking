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
import java.time.OffsetDateTime;
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
    private final clyvasync.Clyvasync.repository.booking.BookingRepository bookingRepository;
    private final clyvasync.Clyvasync.repository.homestay.ReviewImageRepository reviewImageRepository;

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

    @Override
    public Double getAverageRatingByHomestaysUpToDate(List<Long> homestayIds, OffsetDateTime targetDate) {
        if (homestayIds == null || homestayIds.isEmpty()) return 0.0;
        return reviewRepository.getAverageRatingByHomestaysUpToDate(homestayIds, targetDate);
    }

    @Override
    @Transactional
    public ReviewResponse createReview(Long userId, clyvasync.Clyvasync.dto.request.ReviewCreateRequest request) {
        clyvasync.Clyvasync.modules.booking.entity.Booking booking = bookingRepository.findBookingByBookingCode(request.getBookingCode())
                .orElseThrow(() -> new clyvasync.Clyvasync.exception.AppException(clyvasync.Clyvasync.exception.ResultCode.BOOKING_NOT_FOUND));

        if (!booking.getUserId().equals(userId)) {
            throw new clyvasync.Clyvasync.exception.AppException(clyvasync.Clyvasync.exception.ResultCode.PERMISSION_DENIED);
        }

        if (!java.util.List.of(
                clyvasync.Clyvasync.enums.booking.BookingStatus.COMPLETED,
                clyvasync.Clyvasync.enums.booking.BookingStatus.CHECKED_IN,
                clyvasync.Clyvasync.enums.booking.BookingStatus.CONFIRMED
        ).contains(booking.getStatus())) {
            throw new clyvasync.Clyvasync.exception.AppException(clyvasync.Clyvasync.exception.ResultCode.BOOKING_REQUIRED_FOR_REVIEW);
        }

        if (reviewRepository.existsByBookingId(booking.getId())) {
            throw new clyvasync.Clyvasync.exception.AppException(clyvasync.Clyvasync.exception.ResultCode.ALREADY_REVIEWED);
        }

        Review review = new Review();
        review.setBookingId(booking.getId());
        review.setHomestayId(booking.getHomestayId());
        review.setGuestId(userId);
        review.setRating(request.getRating());
        review.setComment(request.getContent());
        review = reviewRepository.save(review);

        List<String> imageKeys = request.getImageKeys();
        if (imageKeys != null && !imageKeys.isEmpty()) {
            List<ReviewImage> pendingImages = reviewImageRepository.findByGuestIdAndStatusAndImageUrlIn(
                    userId, clyvasync.Clyvasync.enums.media.MediaStatus.PENDING, imageKeys);
            
            for (ReviewImage image : pendingImages) {
                image.setReviewId(review.getId());
                image.setStatus(clyvasync.Clyvasync.enums.media.MediaStatus.ACTIVE);
            }
            reviewImageRepository.saveAll(pendingImages);
        }

        return reviewMapper.toReviewResponse(review);
    }

    @Override
    public boolean checkReviewEligibility(Long userId, String bookingCode) {
        return bookingRepository.findBookingByBookingCode(bookingCode)
                .filter(b -> b.getUserId().equals(userId))
                .filter(b -> java.util.List.of(
                        clyvasync.Clyvasync.enums.booking.BookingStatus.COMPLETED,
                        clyvasync.Clyvasync.enums.booking.BookingStatus.CHECKED_IN,
                        clyvasync.Clyvasync.enums.booking.BookingStatus.CONFIRMED
                ).contains(b.getStatus()))
                .map(b -> !reviewRepository.existsByBookingId(b.getId()))
                .orElse(false);
    }
}
