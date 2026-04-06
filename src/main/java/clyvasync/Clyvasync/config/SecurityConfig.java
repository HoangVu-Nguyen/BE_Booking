package clyvasync.Clyvasync.config;

import clyvasync.Clyvasync.security.encoder.PepperedPasswordEncoder;
import clyvasync.Clyvasync.security.entrypoint.CustomAuthenticationEntryPoint;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Order(2)
public class SecurityConfig {

//    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//    private final JwtBlacklistFilter jwtBlacklistFilter;
//    private final IpWhitelistFilter ipWhitelistFilter;
//    private final RateLimitingFilter rateLimitingFilter;
//    private final XSSFilter xssFilter;
//    private final ContentSecurityPolicyFilter contentSecurityPolicyFilter;
//    private final RequestLoggingFilter requestLoggingFilter;
//    private final SecurityHeaderFilter securityHeaderFilter;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

//    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
//                          JwtBlacklistFilter jwtBlacklistFilter,
//                          IpWhitelistFilter ipWhitelistFilter,
//                          RateLimitingFilter rateLimitingFilter,
//                          XSSFilter xssFilter,
//                          ContentSecurityPolicyFilter contentSecurityPolicyFilter,
//                          RequestLoggingFilter requestLoggingFilter,
//                          SecurityHeaderFilter securityHeaderFilter) {
//        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
//        this.jwtBlacklistFilter = jwtBlacklistFilter;
//        this.ipWhitelistFilter = ipWhitelistFilter;
//        this.rateLimitingFilter = rateLimitingFilter;
//        this.xssFilter = xssFilter;
//        this.contentSecurityPolicyFilter = contentSecurityPolicyFilter;
//        this.requestLoggingFilter = requestLoggingFilter;
//        this.securityHeaderFilter = securityHeaderFilter;
//    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable()) // Disable để test localhost cho dễ

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/error", "/oauth2/**", "/.well-known/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // Cho phép các API của mày (Resource Server mode)
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/ws/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        .anyRequest().authenticated()
                )

                // Cấu hình Form Login
                .formLogin(form -> form
                                .loginPage("/login") // Mày dùng trang login tự chế hoặc mặc định
                                .loginProcessingUrl("/login")
                                .permitAll()

                )

                // Cấu hình để file này cũng hiểu JWT gửi lên các API
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                );

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(@Value("${app.security.password.pepper}") String pepper) {
        // Thông số Argon2id chuẩn bảo mật cao:
        // saltLength: 16 byte
        // hashLength: 32 byte
        // parallelism: 4 (số luồng CPU xử lý song song)
        // memory: 65536 (ngốn 64MB RAM cho mỗi lần băm)
        // iterations: 3 (lặp 3 vòng băm)
        Argon2PasswordEncoder argon2 = new Argon2PasswordEncoder(16, 32, 4, 65536, 3);

        // Trả về bộ mã hóa kép
        return new PepperedPasswordEncoder(argon2, pepper);
    }
    @Bean
    public JwtDecoder jwtDecoder(com.nimbusds.jose.jwk.source.JWKSource<com.nimbusds.jose.proc.SecurityContext> jwkSource) {
        NimbusJwtDecoder jwtDecoder = (NimbusJwtDecoder) org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration.jwtDecoder(jwkSource);

        // Cấu hình Validator với khoảng bù thời gian (Clock Skew)
        OAuth2TokenValidator<Jwt> withClockSkew = new DelegatingOAuth2TokenValidator<>(
                new JwtTimestampValidator(java.time.Duration.ofSeconds(60)), // Bù 60s lệch giờ
                new JwtIssuerValidator("https://localhost:8443")
        );

        jwtDecoder.setJwtValidator(withClockSkew);
        return jwtDecoder;
    }
}