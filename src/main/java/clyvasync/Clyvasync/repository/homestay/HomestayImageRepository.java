package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.HomestayImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomestayImageRepository extends JpaRepository<HomestayImage,Long> {
    List<HomestayImage> findAllByHomestayId(Long homestayId);
    List<HomestayImage> findAllByHomestayIdIn(List<Long> homestayIds);
    List<HomestayImage> findByHomestayIdInOrderByDisplayOrderAsc(List<Long> homestayIds);
    List<HomestayImage> findByHomestayIdOrderByDisplayOrderAsc(Long homestayId);
}
