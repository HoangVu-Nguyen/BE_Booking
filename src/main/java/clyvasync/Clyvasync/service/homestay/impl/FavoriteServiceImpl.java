package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.modules.homestay.entity.HomestayFavorite;
import clyvasync.Clyvasync.repository.homestay.HomestayFavoriteRepository;
import clyvasync.Clyvasync.service.homestay.FavoriteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class FavoriteServiceImpl implements FavoriteService {
    private final HomestayFavoriteRepository favoriteRepository;
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

}
