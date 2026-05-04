package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.HomestayImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HomestayPrice extends JpaRepository<HomestayImage,Long> {
}
