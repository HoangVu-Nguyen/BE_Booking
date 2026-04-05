package clyvasync.Clyvasync.service.cache.impl;

import clyvasync.Clyvasync.enums.cache.RedisKeyType;
import clyvasync.Clyvasync.service.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheServiceImpl implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void save(String key, Object value, Duration ttl) {
        redisTemplate.opsForValue().set(key, value, ttl);
    }

    @Override
    public Object get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public void saveOtp(String email, String otp, RedisKeyType type) {
        String finalKey = type.name() + ":" + email;
        this.save(finalKey, otp, Duration.ofMinutes(15));
        log.info("Đã lưu OTP vào Redis cho email: {} với key: {}", email, finalKey);
    }

    @Override
    public String getOtp(String email, RedisKeyType type) {
        String finalKey = type.name() + ":" + email;
        return (String) this.get(finalKey);
    }

    @Override
    public boolean hasKey(String email, RedisKeyType type) {
        return redisTemplate.hasKey(type.name() + ":" + email);
    }
    @Override
    public boolean isSpamming(String email, RedisKeyType type) {
        return redisTemplate.hasKey(type.getFullKey(email));
    }

    @Override
    public void setProcessLimit(String email, RedisKeyType type) {
        String key = type.getFullKey(email);
        // Lưu một giá trị bất kỳ, quan trọng là cái TTL 60s
        redisTemplate.opsForValue().set(key, "blocked", type.getDefaultTtl(), type.getTimeUnit());
    }
}
