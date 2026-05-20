package clyvasync.Clyvasync.repository.media;

import clyvasync.Clyvasync.dto.projection.UserAvatarProjection;
import clyvasync.Clyvasync.enums.media.ImageType;
import clyvasync.Clyvasync.modules.media.entity.UserPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserPhotoRepository extends JpaRepository<UserPhoto,Long> {
    Optional<UserPhoto> findFirstByUserIdAndPhotoTypeAndIsCurrentTrue(Long userId, ImageType photoType);
    @Query("SELECT up.userId AS userId, up.photoUrl AS photoUrl " +
            "FROM UserPhoto up " +
            "WHERE up.userId IN :userIds AND up.photoType = :photoType AND up.isCurrent = true")
    List<UserAvatarProjection> findAvatarsByUserIds(
            @Param("userIds") List<Long> userIds,
            @Param("photoType") ImageType photoType
    );
}
