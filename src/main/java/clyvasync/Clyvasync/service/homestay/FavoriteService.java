package clyvasync.Clyvasync.service.homestay;

public interface FavoriteService {
    boolean toggleFavorite(Long userId, Long homestayId);
}
