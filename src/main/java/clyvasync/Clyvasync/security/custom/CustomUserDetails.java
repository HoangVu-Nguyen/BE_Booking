package clyvasync.Clyvasync.security.custom;

import clyvasync.Clyvasync.entity.auth.User;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.stream.Collectors;

@Getter // Để các lớp Config gọi được .getId(), .getEmail()
public class CustomUserDetails implements UserDetails {

    private final Long id;
    private final String email;
    private final String password;
    private final boolean isActive; // BƯỚC 1: Thêm biến lưu trạng thái Active
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(User user) {
        this.id = user.getId();
        this.email = user.getEmail();
        this.password = user.getPasswordHash();

        // BƯỚC 2: Gán giá trị thực tế từ Database vào biến
        this.isActive = user.isActive();

        // Map Roles từ Entity sang GrantedAuthority của Spring
        this.authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.getName().name()))
                .collect(Collectors.toList());
    }

    @Override
    public String getUsername() { return email; }

    @Override
    public String getPassword() { return password; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() { return authorities; }

    // Các hàm check trạng thái tài khoản
    @Override public boolean isAccountNonExpired() { return true; }
    @Override public boolean isAccountNonLocked() { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    // BƯỚC 3: Trả về trạng thái thực tế thay vì "true"
    @Override
    public boolean isEnabled() {
        return isActive;
    }
}