package clyvasync.Clyvasync.security.validator;


import clyvasync.Clyvasync.service.cache.CacheService;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jwt.Jwt;

public class JwtBlacklistValidator implements OAuth2TokenValidator<Jwt> {
    private final CacheService cacheService;

    public JwtBlacklistValidator(CacheService cacheService) {
        this.cacheService = cacheService;
    }

    @Override
    public OAuth2TokenValidatorResult validate(Jwt jwt) {
        // Kiểm tra xem token value có nằm trong blacklist Redis không
        String tokenValue = jwt.getTokenValue();
        if (cacheService.hasKey("blacklist:" + tokenValue)) {
            return OAuth2TokenValidatorResult.failure(
                    new OAuth2Error("token_blacklisted", "Phiên đăng nhập đã hết hạn hoặc đã đăng xuất!", null)
            );
        }
        return OAuth2TokenValidatorResult.success();
    }
}