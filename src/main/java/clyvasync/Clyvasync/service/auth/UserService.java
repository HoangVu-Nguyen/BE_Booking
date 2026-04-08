package clyvasync.Clyvasync.service.auth;

import clyvasync.Clyvasync.modules.auth.entity.User;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public interface UserService {
    // =====================================================================
    // 1. TÌM KIẾM THEO ĐỊNH DANH (IDENTIFICATION)
    // =====================================================================

    User getUserByEmail(String email);

    Optional<User> findById(Long userId);
    Optional<User> findOptionalByEmail(String email);

    boolean existsById(Long userId);

    /**
     * Tìm User (ưu tiên lấy từ Cache, nếu không có mới gọi DB).
     * Tên cũ: getUserFromCacheOrDb, getUserByEmail
     */
    Optional<User> findByEmailWithCache(String email);

    /**
     * Lấy danh sách ID của User theo FullName.
     * Tên cũ: findIdsByFullName
     */
    List<Long> findUserIdsByFullName(String fullName);

//    /**
//     * Lấy danh sách ID của User theo Username.
//     * Tên cũ: findIdsByUsername
//     */
//    List<UserChatDTO> findUserIdsByUsername(String username);
//
//    Optional<String> findUsernameById(Long userId);
//
//
//    // =====================================================================
//    // 2. THAO TÁC CƠ BẢN (CRUD & STATE)
//    // =====================================================================
//
    User save(User user);
//
//    /**
//     * Tạo mới User từ request đăng ký.
//     * Tên cũ: createUser, addUser
//     */
//    User createUser(RegisterRequest request);
//
//    /**
//     * Cập nhật thông tin User.
//     * Tên cũ: updateUser
//     */
//    User updateUserInfo(String email, Boolean active, String name, String hashPassword);
//
//    /**
//     * Cập nhật FullName.
//     * Tên cũ: updateFullName
//     */
//    boolean updateFullName(Long userId, String newFirstName, String newMiddleName, String newLastName);
//
//    boolean isActiveUser(User user);
//
//    boolean checkActiveAccount(String email);
//
//
//    // =====================================================================
//    // 3. TÌM KIẾM MỞ RỘNG (QUERIES & SEARCH)
//    // =====================================================================
//
//    /**
//     * Tên cũ: getAllUsers
//     */
//    List<UserDTO> findAllUsers();
//
//    Optional<UserDTO> findUserDTOById(Long userId);
//
//    List<User> findAllUsersByIds(List<Long> userIds);
//
//    /**
//     * Tên cũ: searchUsersFuzzy
//     */
//    List<Object[]> searchUsersFuzzy(String username, Long viewId);
//
//
//    // =====================================================================
//    // 4. THÔNG TIN TRUY XUẤT PHỨC TẠP (COMPLEX FETCHING)
//    // =====================================================================
//
//    UserChatDTO findUserChatByUserId(String userId);
//
//    /**
//     * Tên cũ: findAllUsersAndRelationshipsByIds
//     */
//    List<UserProfilePreviewDTO> findUsersAndRelationships(String userId, List<Long> targetUserIds);
//
//    List<UserProfilePreviewDTO> findUserProfilePreviewsByIds(List<Long> ids);
//
//    UserStatusDTO findUserStatus(String userId);
//
//    Ringtone findRingtoneByUserId(Long userId);
//
//    Optional<UserContactInfoDTO> findContactInfoById(Long id);
//
//
//    // =====================================================================
//    // 5. MAPPING & SUMMARY QUERIES (Truy vấn trả về Map)
//    // =====================================================================
//
//    Map<Long, UserDTO> mapUsersByIds(List<Long> userIds);
//
//    Map<String, String> mapUsernamesByEmails(List<String> emails);
//
//    Map<Long, String> mapUsernamesByIds(List<Long> ids);
//
//    List<InforDTO> findUserInfosByEmails(List<String> emails);
//
//    List<InforDTO> findUserInfosByIds(List<Long> ids);
//
//    Map<Long, UserDTO> findUsersSummaryMapByIds(List<Long> ids);
//
//    Optional<UserDTO> findUserSummaryById(Long userId);
}
