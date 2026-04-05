package clyvasync.Clyvasync.service.cache.impl;

import clyvasync.Clyvasync.enums.cache.RedisKeyType;
import clyvasync.Clyvasync.service.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

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
    @Override
    public void increaseFailedAttempts(String email) {
        String key = RedisKeyType.FAILED_ATTEMPTS.getFullKey(email);
        Long attempts = redisTemplate.opsForValue().increment(key);

        // 1. Nếu là lần đầu tiên sai, đặt TTL (ví dụ 30 phút) để reset đếm sau một khoảng thời gian
        if (attempts != null && attempts == 1) {
            redisTemplate.expire(key, RedisKeyType.FAILED_ATTEMPTS.getDefaultTtl(), RedisKeyType.FAILED_ATTEMPTS.getTimeUnit());
        }

        // 2. LOGIC QUAN TRỌNG: Nếu đạt ngưỡng 10 lần, thực hiện khóa tài khoản
        if (attempts != null && attempts >= 10) {
            log.warn("Tài khoản {} đã nhập sai {} lần. Tiến hành khóa 24h.", email, attempts);
            this.lockAccount(email); // Gọi hàm này để tạo key BLOCK_LOGIN

            // Tùy chọn: Xóa luôn key đếm để sau 24h họ bắt đầu lại từ 0
            this.resetFailedAttempts(email);
        }
    }

    @Override
    public boolean isBruteForce(String email) {
        String key = RedisKeyType.FAILED_ATTEMPTS.getFullKey(email);
        Object val = redisTemplate.opsForValue().get(key);

        if (val == null) return false;

        try {
            // Redis increment thường trả về Long hoặc Integer tùy Serializer
            int attempts = Integer.parseInt(val.toString());
            return attempts >= 5;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    @Override
    public void resetFailedAttempts(String email) {
        redisTemplate.delete(RedisKeyType.FAILED_ATTEMPTS.getFullKey(email));
    }
    @Override
    public void lockAccount(String email) {
        String key = RedisKeyType.BLOCK_LOGIN.getFullKey(email);
        redisTemplate.opsForValue().set(key, "locked", 24, TimeUnit.HOURS);
    }

    @Override
    public boolean isAccountLocked(String email) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeyType.BLOCK_LOGIN.getFullKey(email)));
    }
}
