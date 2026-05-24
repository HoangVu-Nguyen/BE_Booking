package clyvasync.Clyvasync.security.aspect;

import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import clyvasync.Clyvasync.service.homestay.HomestayService;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class SecurityAspect {
    private final HomestayService homestayService;

    @Before("@annotation(clyvasync.Clyvasync.service.annotation.IsHomestayOwner) && args(id, ..)")
    public void checkOwnership(Long id) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ResultCode.UNAUTHENTICATED);
        }

        if (!(authentication.getPrincipal() instanceof Jwt)) {
            throw new AppException(ResultCode.UNAUTHENTICATED);
        }

        Jwt jwt = (Jwt) authentication.getPrincipal();

        // 3. Lấy userId một cách an toàn nhất (chống lỗi ép kiểu từ JSON)
        Object userIdClaim = jwt.getClaim("user_id");
        if (userIdClaim == null) {
            throw new AppException(ResultCode.UNAUTHENTICATED);
        }

        // Parse ra Long đảm bảo cùng kiểu với ownerId trong Database
        Long currentUserId = Long.valueOf(userIdClaim.toString());

        // 4. Lấy Homestay lên
        Homestay homestay = homestayService.findById(id);

        // 5. So sánh 2 biến kiểu Long: Nếu KHÔNG PHẢI chủ nhà thì cấm!
        if (!homestay.getOwnerId().equals(currentUserId)) {
            throw new AppException(ResultCode.ACCESS_DENIED);
        }
    }
}