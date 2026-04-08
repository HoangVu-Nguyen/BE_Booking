package clyvasync.Clyvasync.service.media.impl;

import clyvasync.Clyvasync.dto.request.UploadPhotoRequest;
import clyvasync.Clyvasync.dto.response.AvatarResponse;
import clyvasync.Clyvasync.dto.response.UserPhotoResponse;
import clyvasync.Clyvasync.enums.media.ImageType;
import clyvasync.Clyvasync.modules.auth.entity.User;
import clyvasync.Clyvasync.modules.media.entity.UserPhoto;
import clyvasync.Clyvasync.repository.media.UserPhotoRepository;
import clyvasync.Clyvasync.service.media.IUserPhotoService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
@Service
@RequiredArgsConstructor
public class UserPhotoServiceImpl implements IUserPhotoService {
    private final UserPhotoRepository userPhotoRepository;
    @Override
    public UserPhotoResponse getCurrentPhoto(Long userId, ImageType imageType) {
        Optional<UserPhoto> pUserPhoto = userPhotoRepository
                .findFirstByUserIdAndPhotoTypeAndIsCurrentTrue(userId, imageType);

        return null;
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
}
