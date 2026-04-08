package clyvasync.Clyvasync.service.media;

import clyvasync.Clyvasync.dto.request.UploadPhotoRequest;
import clyvasync.Clyvasync.dto.response.AvatarResponse;
import clyvasync.Clyvasync.dto.response.UserPhotoResponse;
import clyvasync.Clyvasync.enums.media.ImageType;
import clyvasync.Clyvasync.modules.auth.entity.User;
import clyvasync.Clyvasync.modules.media.entity.UserPhoto;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

public interface IUserPhotoService {
// ==========================================
    // NHÓM 1: CÁC HÀM TRẢ VỀ DTO CHO CONTROLLER
    // (Dùng để giao tiếp với Frontend)
    // ==========================================

    UserPhotoResponse getCurrentPhoto(Long userId, ImageType imageType);

    List<UserPhotoResponse> getAllPhotos(Long userId);

    // Gộp saveAvatar và saveCover thành 1 hàm duy nhất cho gọn
    UserPhotoResponse savePhoto(UploadPhotoRequest uploadPhotoRequest, Long userId);

    List<UserPhotoResponse> getTop6RecentPhotos(Long userId);

    List<UserPhotoResponse> getCurrentPhotosByTypes(Long userId, List<ImageType> imageTypes);

    List<UserPhotoResponse> getTop6AvatarAndCover(Long userId);

    // ==========================================
    // NHÓM 2: CÁC HÀM TRẢ VỀ ENTITY VÀ DỮ LIỆU THÔ
    // (Dùng nội bộ giữa các Service với nhau)
    // ==========================================

    // Đã bỏ Optional<User> đi, truyền trực tiếp User vào nếu cần
    UserPhoto saveUserPhotoEntity(Long userId, UploadPhotoRequest uploadPhotoRequest, ImageType imageType, User user);

    List<UserPhoto> getCurrentPhotosByIds(Set<String> userIds);

    List<UserPhoto> getTop6RecentPhotos();

    void updatePhotoUrl(Long photoId, Long userId, String newPhotoUrl);

    UserPhoto getPhotoEntityById(Long photoId);

    Optional<UserPhoto> getCurrentPhotoEntity(Long userId, ImageType imageType);

    List<UserPhoto> getCurrentAvatarsEntities(List<Long> userIds, ImageType imageType);

    String getCurrentAvatarUrl(Long userId);

    Map<Long, String> getAvatarsMapByIds(List<Long> userIds);

    List<AvatarResponse> getCurrentAvatarsByEmails(List<String> emails, ImageType imageType); // Đã đổi tên thành AvatarResponse

    Map<String, String> getAvatarsMapByEmails(List<String> emails);

    Optional<String> getAvatarUrlOpt(Long userId);
}
