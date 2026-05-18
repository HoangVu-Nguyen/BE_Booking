package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.HomestayFavorite;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HomestayFavoriteRepository extends JpaRepository<HomestayFavorite,Long> {
    Optional<HomestayFavorite> findByUserIdAndHomestayId(Long userId, Long homestayId);

    void deleteByUserIdAndHomestayId(Long userId, Long homestayId);
}
