package clyvasync.Clyvasync.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable) // Tắt CSRF (Bắt buộc khi làm API với Postman)
                .authorizeHttpRequests(auth -> auth
                        // CẤP QUYỀN VÀO TỰ DO CHO CÁC API AUTH
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        // Cấp quyền cho Swagger (nếu bạn có dùng)
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Các API khác bắt buộc phải đăng nhập
                        .anyRequest().authenticated()
                );
        return http.build();
    }


    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}