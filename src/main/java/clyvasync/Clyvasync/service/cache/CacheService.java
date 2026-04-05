package clyvasync.Clyvasync.service.cache;


import clyvasync.Clyvasync.enums.cache.RedisKeyType;

import java.time.Duration;

public interface CacheService {
    void save(String key, Object value, Duration ttl);
    Object get(String key);
    void delete(String key);

    void saveOtp(String email, String otp, RedisKeyType type);
    String getOtp(String email, RedisKeyType type);
    boolean hasKey(String email, RedisKeyType type);
    boolean isSpamming(String email, RedisKeyType type);
    void setProcessLimit(String email, RedisKeyType type);
}
