package clyvasync.Clyvasync.service.auth.impl;

import clyvasync.Clyvasync.constant.NameConstants;
import clyvasync.Clyvasync.dto.response.UserHeaderResponse;
import clyvasync.Clyvasync.dto.response.UserPhotoResponse;
import clyvasync.Clyvasync.enums.media.ImageType;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.auth.entity.User;
import clyvasync.Clyvasync.repository.auth.UserRepository;
import clyvasync.Clyvasync.repository.projection.UserNameProjection;
import clyvasync.Clyvasync.service.auth.UserService;
import clyvasync.Clyvasync.service.media.IUserPhotoService;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final IUserPhotoService userPhotoService;
    @Override
    public User getUserByEmail(String email) {
        log.debug("Đang truy vấn thông tin user với email: {}", email);
        return userRepository.findByEmail(email).orElseThrow(()-> new AppException(ResultCode.USER_NOT_FOUND));
    }

    @Override
    public Optional<User> findById(Long userId) {
        return null;
    }

    @Override
    public Optional<User> findOptionalByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    @Override
    public boolean existsById(Long userId) {
        return false;
    }

    @Override
    public Optional<User> findByEmailWithCache(String email) {
        return null;
    }

    @Override
    public List<Long> findUserIdsByFullName(String fullName) {
        return null;
    }

//    @Override
//    public List<UserChatDTO> findUserIdsByUsername(String username) {
//        return null;
//    }
//
//    @Override
//    public Optional<String> findUsernameById(Long userId) {
//        return null;
//    }
//
    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
    @Cached(name = "userHeaderCache-", key = "#userId", cacheType = CacheType.BOTH, expire = 600)
    @Override
    public UserHeaderResponse getHeaderInfo(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new AppException(ResultCode.USER_NOT_FOUND);
        }

        UserNameProjection user = userRepository.findProjectedById(userId).orElse(null);

        String username = (user != null) ? user.getUsername() : NameConstants.NAME_DEFAULT;

        UserPhotoResponse photoResponse = userPhotoService.getCurrentPhoto(userId, ImageType.AVATAR);

        return UserHeaderResponse.builder()
                .id(userId)
                .username(username)
                .photoUrl(photoResponse.getPhotoUrl())
                .build();
    }
//
//    @Override
//    public User createUser(RegisterRequest request) {
//        return null;
//    }
//
//    @Override
//    public User updateUserInfo(String email, Boolean active, String name, String hashPassword) {
//        return null;
//    }
//
//    @Override
//    public boolean updateFullName(Long userId, String newFirstName, String newMiddleName, String newLastName) {
//        return false;
//    }
//
//    @Override
//    public boolean isActiveUser(User user) {
//        return false;
//    }
//
//    @Override
//    public boolean checkActiveAccount(String email) {
//        return false;
//    }
//
//    @Override
//    public List<UserDTO> findAllUsers() {
//        return null;
//    }
//
//    @Override
//    public Optional<UserDTO> findUserDTOById(Long userId) {
//        return null;
//    }
//
//    @Override
//    public List<User> findAllUsersByIds(List<Long> userIds) {
//        return null;
//    }
//
//    @Override
//    public List<Object[]> searchUsersFuzzy(String username, Long viewId) {
//        return null;
//    }
//
//    @Override
//    public UserChatDTO findUserChatByUserId(String userId) {
//        return null;
//    }
//
//    @Override
//    public List<UserProfilePreviewDTO> findUsersAndRelationships(String userId, List<Long> targetUserIds) {
//        return null;
//    }
//
//    @Override
//    public List<UserProfilePreviewDTO> findUserProfilePreviewsByIds(List<Long> ids) {
//        return null;
//    }
//
//    @Override
//    public UserStatusDTO findUserStatus(String userId) {
//        return null;
//    }
//
//    @Override
//    public Ringtone findRingtoneByUserId(Long userId) {
//        return null;
//    }
//
//    @Override
//    public Optional<UserContactInfoDTO> findContactInfoById(Long id) {
//        return null;
//    }
//
//    @Override
//    public Map<Long, UserDTO> mapUsersByIds(List<Long> userIds) {
//        return null;
//    }
//
//    @Override
//    public Map<String, String> mapUsernamesByEmails(List<String> emails) {
//        return null;
//    }
//
//    @Override
//    public Map<Long, String> mapUsernamesByIds(List<Long> ids) {
//        return null;
//    }
//
//    @Override
//    public List<InforDTO> findUserInfosByEmails(List<String> emails) {
//        return null;
//    }
//
//    @Override
//    public List<InforDTO> findUserInfosByIds(List<Long> ids) {
//        return null;
//    }
//
//    @Override
//    public Map<Long, UserDTO> findUsersSummaryMapByIds(List<Long> ids) {
//        return null;
//    }
//
//    @Override
//    public Optional<UserDTO> findUserSummaryById(Long userId) {
//        return null;
//    }
}
