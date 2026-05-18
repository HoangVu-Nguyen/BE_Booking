package clyvasync.Clyvasync.service.homestay;

import clyvasync.Clyvasync.dto.response.HomestayCardResponse;
import clyvasync.Clyvasync.dto.response.HomestayResponse;

import java.util.List;

public interface FavoriteService {
    boolean toggleFavorite(Long userId, Long homestayId);
    boolean existsHomestayFavoriteByHomestayId(Long homestayId);
    List<HomestayCardResponse> getUserFavoriteHomestays(Long userId);
}
