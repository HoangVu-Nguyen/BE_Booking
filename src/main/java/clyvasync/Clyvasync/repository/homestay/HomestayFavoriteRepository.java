package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.HomestayFavorite;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface HomestayFavoriteRepository extends JpaRepository<HomestayFavorite,Long> {
    Optional<HomestayFavorite> findByUserIdAndHomestayId(Long userId, Long homestayId);

    void deleteByUserIdAndHomestayId(Long userId, Long homestayId);
    boolean existsHomestayFavoriteByHomestayId(Long homestayId);
    @Query("SELECT f.homestayId FROM HomestayFavorite f WHERE f.userId = :userId ORDER BY f.createdAt DESC")
    List<Long> findHomestayIdsByUserId(@Param("userId") Long userId);
}
