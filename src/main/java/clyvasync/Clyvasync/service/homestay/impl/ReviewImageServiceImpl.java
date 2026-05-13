package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.enums.cache.RedisKeyType;
import clyvasync.Clyvasync.modules.homestay.entity.ReviewImage;
import clyvasync.Clyvasync.repository.homestay.ReviewImageRepository;
import clyvasync.Clyvasync.service.cache.CacheService;
import clyvasync.Clyvasync.service.homestay.ReviewImageService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ReviewImageServiceImpl implements ReviewImageService {
    private final ReviewImageRepository reviewImageRepository;
    private final CacheService cacheService;
    @Override
    public Map<Long, List<String>> getImagesForReviews(List<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) return Map.of();

        List<Long> distinctIds = reviewIds.stream().distinct().toList();
        List<String> redisKeys = distinctIds.stream()
                .map(id -> RedisKeyType.REVIEW_IMAGES.getPrefix() + id)
                .toList();

        List<List> cachedImages = cacheService.multiGet(redisKeys, List.class);

        Map<Long, List<String>> resultMap = new HashMap<>();
        List<Long> missingIds = new ArrayList<>();

        for (int i = 0; i < distinctIds.size(); i++) {
            List<String> images = (List<String>) cachedImages.get(i);
            if (images != null) {
                resultMap.put(distinctIds.get(i), images);
            } else {
                missingIds.add(distinctIds.get(i));
            }
        }

        if (!missingIds.isEmpty()) {
            List<ReviewImage> dbImages = reviewImageRepository.findAllByReviewIdIn(missingIds);

            Map<Long, List<String>> missingMap = dbImages.stream().collect(Collectors.groupingBy(
                    ReviewImage::getReviewId,
                    Collectors.mapping(ReviewImage::getImageUrl, Collectors.toList())
            ));

            for (Long id : missingIds) {
                List<String> images = missingMap.getOrDefault(id, List.of());
                resultMap.put(id, images);

                cacheService.save(RedisKeyType.REVIEW_IMAGES.getPrefix() + id, images, Duration.ofHours(1));
            }
        }

        return resultMap;
    }

}
