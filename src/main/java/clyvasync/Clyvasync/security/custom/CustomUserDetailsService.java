package clyvasync.Clyvasync.security.custom;



import clyvasync.Clyvasync.enums.user.UserStatus;
import clyvasync.Clyvasync.modules.auth.entity.User;
import clyvasync.Clyvasync.service.auth.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j // Thêm cái này để dùng biến 'log'
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        log.info("=== BẮT ĐẦU LUỒNG KIỂM TRA ĐĂNG NHẬP ===");
        log.info("1. Spring Security đang yêu cầu tìm User với email: [{}]", email);

        User user = userService.getUserByEmail(email);


        if (user == null) {
            log.error(" THẤT BẠI: Không tìm thấy tài khoản nào trong DB khớp với email [{}]", email);
            throw new UsernameNotFoundException("User not found with email: " + email);
        }
        if (user.getStatus() == UserStatus.SUSPENDED) {
            if (user.getSuspendedUntil() != null) {
                if (LocalDateTime.now().isAfter(user.getSuspendedUntil())) {
                    log.info("Tài khoản {} đã hết hạn đình chỉ. Đang tự động mở khóa...", email);
                    user.setStatus(UserStatus.ACTIVE);
                    user.setSuspendedUntil(null);
                    userService.save(user);

                } else {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
                    String unlockTime = user.getSuspendedUntil().format(formatter);
                    throw new LockedException("Tài khoản bị đình chỉ. Sẽ được mở khóa vào: " + unlockTime);
                }
            } else {
                throw new LockedException("Tài khoản của bạn đã bị đình chỉ hoạt động. Vui lòng liên hệ CSKH.");
            }
        }

        if (user.getStatus() == UserStatus.BANNED) {
            throw new DisabledException("Tài khoản của bạn đã bị khóa vĩnh viễn.");
        }

        log.info("2.  TÌM THẤY USER TRONG DB: ID = {}, Email = {}, Trạng thái (isActive) = {}",
                user.getId(), user.getEmail(), user.isActive());

        log.info("   -> Password Hash trong DB: {}", user.getPasswordHash());

        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        log.info("3. Đã đóng gói xong vào CustomUserDetails. Các quyền (Roles) được load: {}", customUserDetails.getAuthorities());
        log.info("=== TRẢ KẾT QUẢ CHO SPRING TỰ SO SÁNH MẬT KHẨU ===");

        return customUserDetails;
    }
}