package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.enums.media.MediaStatus;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface HomestayImageRepository extends JpaRepository<HomestayImage,Long> {
    List<HomestayImage> findAllByHomestayId(Long homestayId);
    List<HomestayImage> findAllByHomestayIdIn(List<Long> homestayIds);
    List<HomestayImage> findByHomestayIdInOrderByDisplayOrderAsc(List<Long> homestayIds);
    List<HomestayImage> findByHomestayIdOrderByDisplayOrderAsc(Long homestayId);
    List<HomestayImage> findByHomestayId(Long homestayId);
    List<HomestayImage> findByImageUrlIn(List<String> imageUrls);
    @Modifying
    @Query("DELETE FROM HomestayImage h WHERE h.homestayId = :homestayId")
    void deleteByHomestayId(@Param("homestayId") Long homestayId);

    @Modifying
    @Query("DELETE FROM HomestayImage h WHERE h.homestayId = :homestayId AND h.status = 'ACTIVE'")
    void deleteActiveByHomestayId(@Param("homestayId") Long homestayId);

    @Modifying
    @Query("DELETE FROM HomestayImage h WHERE h.homestayId = :homestayId AND h.status = 'ACTIVE' AND h.id NOT IN :ids")
    void deleteActiveByHomestayIdAndIdNotIn(@Param("homestayId") Long homestayId, @Param("ids") Set<Long> ids);
}
