package clyvasync.Clyvasync.service.media.impl;

import clyvasync.Clyvasync.constant.ImageConstants;
import clyvasync.Clyvasync.dto.request.UploadPhotoRequest;
import clyvasync.Clyvasync.dto.response.AvatarResponse;
import clyvasync.Clyvasync.dto.response.UserPhotoResponse;
import clyvasync.Clyvasync.enums.media.ImageType;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.mapper.media.UserPhotoMapper;
import clyvasync.Clyvasync.modules.auth.entity.User;
import clyvasync.Clyvasync.modules.media.entity.UserPhoto;
import clyvasync.Clyvasync.repository.media.UserPhotoRepository;
import clyvasync.Clyvasync.service.media.IUserPhotoService;
import com.alicp.jetcache.anno.CachePenetrationProtect;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserPhotoServiceImpl implements IUserPhotoService {
    private final UserPhotoRepository userPhotoRepository;
    private final UserPhotoMapper userPhotoMapper;
    @Cached(
            name = "userAvatarCache-",
            key = "#userId",
            cacheType = CacheType.BOTH,
            localLimit = 1000,
            expire = 600,
            localExpire = 60,
            timeUnit = TimeUnit.SECONDS
    )
    @CachePenetrationProtect
    @Override
    @Transactional(readOnly = true)
    public UserPhotoResponse getCurrentPhoto(Long userId, ImageType imageType) {
        log.info("🚀 CHẠY VÀO DATABASE ĐỂ LẤY ẢNH CỦA USER ID: {}", userId);
        if (userId == null || imageType == null) {
            log.error("Failed to get current photo: userId or imageType is null");
            throw new AppException(ResultCode.INVALID_INPUT);
        }
        return userPhotoRepository
                .findFirstByUserIdAndPhotoTypeAndIsCurrentTrue(userId, imageType)
                .map(userPhotoMapper::toUserPhotoResponse)
                .orElseGet(() -> {
                    log.debug("Photo not found for userId: {}, returning default for type: {}", userId, imageType);
                    return createUserPhotoResponseDefault(userId, imageType);
                });
    }

    @Override
    public List<UserPhotoResponse> getAllPhotos(Long userId) {
        return List.of();
    }

    @Override
    public UserPhotoResponse savePhoto(UploadPhotoRequest uploadPhotoRequest, Long userId) {
        return null;
    }

    @Override
    public List<UserPhotoResponse> getTop6RecentPhotos(Long userId) {
        return List.of();
    }

    @Override
    public List<UserPhotoResponse> getCurrentPhotosByTypes(Long userId, List<ImageType> imageTypes) {
        return List.of();
    }

    @Override
    public List<UserPhotoResponse> getTop6AvatarAndCover(Long userId) {
        return List.of();
    }

    @Override
    public UserPhoto saveUserPhotoEntity(Long userId, UploadPhotoRequest uploadPhotoRequest, ImageType imageType, User user) {
        return null;
    }

    @Override
    public List<UserPhoto> getCurrentPhotosByIds(Set<String> userIds) {
        return List.of();
    }

    @Override
    public List<UserPhoto> getTop6RecentPhotos() {
        return List.of();
    }

    @Override
    public void updatePhotoUrl(Long photoId, Long userId, String newPhotoUrl) {

    }

    @Override
    public UserPhoto getPhotoEntityById(Long photoId) {
        return null;
    }

    @Override
    public Optional<UserPhoto> getCurrentPhotoEntity(Long userId, ImageType imageType) {
        return Optional.empty();
    }

    @Override
    public List<UserPhoto> getCurrentAvatarsEntities(List<Long> userIds, ImageType imageType) {
        return List.of();
    }

    @Override
    public String getCurrentAvatarUrl(Long userId) {
        return "";
    }

    @Override
    public Map<Long, String> getAvatarsMapByIds(List<Long> userIds) {
        return Map.of();
    }

    @Override
    public List<AvatarResponse> getCurrentAvatarsByEmails(List<String> emails, ImageType imageType) {
        return List.of();
    }

    @Override
    public Map<String, String> getAvatarsMapByEmails(List<String> emails) {
        return Map.of();
    }

    @Override
    public Optional<String> getAvatarUrlOpt(Long userId) {
        return Optional.empty();
    }

    private UserPhotoResponse createUserPhotoResponseDefault(Long userId, ImageType imageType) {
        String defaultUrl = getDefaultUrlByType(imageType);

        return new UserPhotoResponse(null, userId, defaultUrl, imageType, true, LocalDateTime.now());
    }

    private String getDefaultUrlByType(ImageType imageType) {
        return switch (imageType) {
            case AVATAR -> ImageConstants.AVATAR_DEFAULT;
            case COVER -> ImageConstants.COVER_DEFAULT;

        };
    }
}
