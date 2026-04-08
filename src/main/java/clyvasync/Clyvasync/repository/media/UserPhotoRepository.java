package clyvasync.Clyvasync.repository.media;

import clyvasync.Clyvasync.enums.media.ImageType;
import clyvasync.Clyvasync.modules.media.entity.UserPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserPhotoRepository extends JpaRepository<UserPhoto,Long> {
    Optional<UserPhoto> findFirstByUserIdAndPhotoTypeAndIsCurrentTrue(Long userId, ImageType photoType);
}
