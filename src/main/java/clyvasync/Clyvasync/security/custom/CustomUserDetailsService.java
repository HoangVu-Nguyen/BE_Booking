package clyvasync.Clyvasync.security.custom;



import clyvasync.Clyvasync.modules.auth.entity.User;
import clyvasync.Clyvasync.service.auth.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        log.info("2.  TÌM THẤY USER TRONG DB: ID = {}, Email = {}, Trạng thái (isActive) = {}",
                user.getId(), user.getEmail(), user.isActive());

        // Mày có thể log luôn cục password bị băm ra để check (nhưng cẩn thận lộ hàng nếu code lên production)
        log.info("   -> Password Hash trong DB: {}", user.getPasswordHash());

        // Đóng gói vào CustomUserDetails
        CustomUserDetails customUserDetails = new CustomUserDetails(user);

        log.info("3. Đã đóng gói xong vào CustomUserDetails. Các quyền (Roles) được load: {}", customUserDetails.getAuthorities());
        log.info("=== TRẢ KẾT QUẢ CHO SPRING TỰ SO SÁNH MẬT KHẨU ===");

        return customUserDetails;
    }
}