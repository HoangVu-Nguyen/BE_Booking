package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.HomestayImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomestayImageRepository extends JpaRepository<HomestayImage,Long> {
    List<HomestayImage> findByHomestayIdOrderByDisplayOrderAsc(Long id);
}
