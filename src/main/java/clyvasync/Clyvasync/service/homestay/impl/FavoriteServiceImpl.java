package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.constant.ImageConstants;
import clyvasync.Clyvasync.dto.response.HomestayCardResponse;
import clyvasync.Clyvasync.dto.summary.HomestayRoomSummary;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayFavorite;
import clyvasync.Clyvasync.modules.homestay.entity.Location;
import clyvasync.Clyvasync.repository.homestay.HomestayFavoriteRepository;
import clyvasync.Clyvasync.service.homestay.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FavoriteServiceImpl implements FavoriteService {

    private final HomestayFavoriteRepository favoriteRepository;
    private final HomestayService homestayService;
    private final LocationService locationService;
    private final HomestayImageService homestayImageService;
    private final HomestayRoomService homestayRoomService;

    public FavoriteServiceImpl(HomestayFavoriteRepository favoriteRepository, @Lazy HomestayService homestayService, LocationService locationService, HomestayImageService homestayImageService, HomestayRoomService homestayRoomService) {
        this.favoriteRepository = favoriteRepository;
        this.homestayService = homestayService;
        this.locationService = locationService;
        this.homestayImageService = homestayImageService;
        this.homestayRoomService = homestayRoomService;
    }

    @Override
    public boolean toggleFavorite(Long userId, Long homestayId) {
        Optional<HomestayFavorite> existingFavorite = favoriteRepository.findByUserIdAndHomestayId(userId, homestayId);
        if (existingFavorite.isPresent()) {
            favoriteRepository.delete(existingFavorite.get());
            log.info("[FAVORITE SERVICE] Removed Homestay {} from User {}'s favorites.", homestayId, userId);
            return false;
        } else {
            HomestayFavorite newFavorite = HomestayFavorite.builder()
                    .userId(userId)
                    .homestayId(homestayId)
                    .build();
            favoriteRepository.save(newFavorite);
            log.info("[FAVORITE SERVICE] Added Homestay {} to User {}'s favorites.", homestayId, userId);
            return true;
        }
    }

    @Override
    public boolean existsHomestayFavoriteByHomestayId(Long homestayId) {
        return favoriteRepository.existsHomestayFavoriteByHomestayId(homestayId);
    }

    @Override
    public List<HomestayCardResponse> getUserFavoriteHomestays(Long userId) {
        log.info("[FAVORITE SERVICE] Request received to retrieve favorite homestays for user: {}", userId);

        // 1. Kéo danh sách ID Homestay yêu thích của User
        List<Long> favoriteHomestayIds = favoriteRepository.findHomestayIdsByUserId(userId);
        if (favoriteHomestayIds.isEmpty()) {
            log.info("[FAVORITE SERVICE] User {} has no favorite homestays.", userId);
            return List.of();
        }

        // 2. Kéo thông tin thô của Homestay Entities theo lô (Batch Load 1)
        List<Homestay> homestayEntities = homestayService.findByIdIn(favoriteHomestayIds);

        // 3. Gom danh sách location_id phục vụ Map Join
        List<Integer> locationIds = homestayEntities.stream()
                .map(Homestay::getLocationId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();


        Map<Integer, String> cityMap = Collections.emptyMap();
        if (!locationIds.isEmpty()) {
            cityMap = locationService.findAllByIds(locationIds).stream()
                    .collect(Collectors.toMap(Location::getId, Location::getCityName, (existing, replacement) -> existing));
        }

        List<HomestayRoomSummary> summaries = homestayRoomService.getRoomSummaries(favoriteHomestayIds);
        Map<Long, HomestayRoomSummary> roomSummaryMap = summaries.stream()
                .collect(Collectors.toMap(HomestayRoomSummary::getHomestayId, s -> s, (existing, replacement) -> existing));


        Map<Long, List<String>> imageMap = homestayImageService.getImagesForHomestays(favoriteHomestayIds);

        final Map<Integer, String> finalCityMap = cityMap;
        final Map<Long, HomestayRoomSummary> finalRoomSummaryMap = roomSummaryMap;
        final Map<Long, List<String>> finalImageMap = imageMap != null ? imageMap : Collections.emptyMap();

        return homestayEntities.stream()
                .map(entity -> {
                    HomestayRoomSummary roomSummary = finalRoomSummaryMap.get(entity.getId());

                    List<String> images = finalImageMap.getOrDefault(entity.getId(), List.of());
                    if (images.isEmpty()) {
                        images = List.of(ImageConstants.TOUR_DEFAULT);
                    }

                    return HomestayCardResponse.builder()
                            .id(entity.getId())
                            .name(entity.getName())
                            .cityName(finalCityMap.getOrDefault(entity.getLocationId(), "Unknown Location"))
                            .basePrice(roomSummary != null ? roomSummary.getMinPrice() : BigDecimal.ZERO)
                            .status(entity.getStatus())
                            .imageUrls(images)
                            .averageRating(entity.getAverageRating())
                            .isFavorite(true)
                            .build();
                })
                .toList();
    }
}