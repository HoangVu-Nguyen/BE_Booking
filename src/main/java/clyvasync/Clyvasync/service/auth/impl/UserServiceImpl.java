package clyvasync.Clyvasync.service.auth.impl;

import clyvasync.Clyvasync.constant.NameConstants;
import clyvasync.Clyvasync.dto.response.OwnerResponse;
import clyvasync.Clyvasync.dto.response.UserHeaderResponse;
import clyvasync.Clyvasync.dto.response.UserPhotoResponse;
import clyvasync.Clyvasync.enums.cache.RedisKeyType;
import clyvasync.Clyvasync.enums.media.ImageType;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.auth.entity.User;
import clyvasync.Clyvasync.repository.auth.UserRepository;
import clyvasync.Clyvasync.repository.projection.UserNameProjection;
import clyvasync.Clyvasync.service.auth.UserService;
import clyvasync.Clyvasync.service.cache.CacheService;
import clyvasync.Clyvasync.service.media.IUserPhotoService;
import com.alicp.jetcache.anno.CacheType;
import com.alicp.jetcache.anno.Cached;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final IUserPhotoService userPhotoService;
    private final CacheService cacheService;



    @Override
    @Transactional(readOnly = true)
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
    @Cacheable(value = "user_info", key = "#userId")
    public OwnerResponse getOwnerInfo(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(()-> new AppException(ResultCode.USER_NOT_FOUND));
        return OwnerResponse.builder().id(userId).isVerified(user.isActive()).joinedAt(user.getCreatedAt()).phoneNumber(user.getPhoneNumber()).fullName(user.getUsername()).avatar(userPhotoService.getCurrentPhoto(userId,ImageType.AVATAR).getPhotoUrl()).build();
    }

    @Override
    public Map<Long, OwnerResponse> getOwnerInfos(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) return Map.of();

        List<Long> distinctIds = userIds.stream().distinct().toList();
        Map<Long, OwnerResponse> resultMap = new HashMap<>();
        List<Long> missingIds = new ArrayList<>();

        for (Long id : distinctIds) {
            OwnerResponse cachedUser = cacheService.get(RedisKeyType.USER_INFO.getPrefix() + id, OwnerResponse.class);
            if (cachedUser != null) {
                resultMap.put(id, cachedUser);
            } else {
                missingIds.add(id);
            }
        }
        if (!missingIds.isEmpty()) {
            List<User> usersFromDb = userRepository.findAllById(missingIds);
            Map<Long, String> userPhotoResponses = userPhotoService.getAvatarsMapByIds(missingIds);

            usersFromDb.forEach(user -> {
                OwnerResponse response = mapToResponse(user, userPhotoResponses.get(user.getId()));
                resultMap.put(user.getId(), response);
                cacheService.save(RedisKeyType.USER_INFO.getPrefix() + user.getId(), response, Duration.ofHours(1));
            });
        }

        return resultMap;
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

        UserNameProjection user = userRepository.findProjectedById(userId).orElseThrow(() -> new AppException(ResultCode.USER_NOT_FOUND));

        String username = (user != null) ? user.getUsername() : NameConstants.NAME_DEFAULT;

        UserPhotoResponse photoResponse = userPhotoService.getCurrentPhoto(userId, ImageType.AVATAR);

        return UserHeaderResponse.builder()
                .id(userId)
                .username(username)
                .photoUrl(photoResponse.getPhotoUrl())
                .createdAt(user.getCreatedAt())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
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
private OwnerResponse mapToResponse(User user, String avatarUrl) {
    return OwnerResponse.builder()
            .id(user.getId())
            .fullName(user.getUsername())
            .avatar(avatarUrl)
            .isVerified(user.isActive())
            .joinedAt(user.getCreatedAt())
            .phoneNumber(user.getPhoneNumber())
            .build();
}
}
