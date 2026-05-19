package clyvasync.Clyvasync.security.aspect;

import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import clyvasync.Clyvasync.service.annotation.CurrentUserId;


import clyvasync.Clyvasync.service.homestay.HomestayService;
import com.sun.security.auth.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class SecurityAspect {
    private final HomestayService homestayService;

    @Before("@annotation(clyvasync.Clyvasync.service.annotation.IsHomestayOwner) && args(id, ..)")
    public void checkOwnership(Long id) {
        // Lấy UserId từ SecurityContext thay vì truyền qua args
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AppException(ResultCode.UNAUTHENTICATED);
        }

        // Giả sử bác lưu userId trong principal (tùy vào cách bác setup JWT)
        Long currentUserId = Long.valueOf(((UserPrincipal) authentication.getPrincipal()).getName());

        Homestay homestay = homestayService.findById(id);

        if (!homestay.getOwnerId().equals(currentUserId)) {
            throw new AppException(ResultCode.ACCESS_DENIED);
        }
    }
}
