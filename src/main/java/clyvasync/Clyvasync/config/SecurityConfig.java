package clyvasync.Clyvasync.config;

import clyvasync.Clyvasync.security.custom.CustomAuthenticationFailureHandler;
import clyvasync.Clyvasync.security.custom.CustomUserDetails;
import clyvasync.Clyvasync.security.custom.CustomUserDetailsMixin;
import clyvasync.Clyvasync.security.encoder.PepperedPasswordEncoder;
import clyvasync.Clyvasync.security.entrypoint.CustomAuthenticationEntryPoint;
import clyvasync.Clyvasync.security.filter.RateLimitingFilter;
import clyvasync.Clyvasync.security.filter.RefreshTokenCookieFilter;
import clyvasync.Clyvasync.security.filter.RequestLoggingFilter;
import clyvasync.Clyvasync.security.filter.XSSFilter;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.authorization.InMemoryOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.JdbcOAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.OAuth2AuthorizationService;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.jackson2.OAuth2AuthorizationServerJackson2Module;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Order(2)
@RequiredArgsConstructor
public class SecurityConfig {

//    private final JwtAuthenticationFilter jwtAuthenticationFilter;
//    private final JwtBlacklistFilter jwtBlacklistFilter;
//    private final IpWhitelistFilter ipWhitelistFilter;
    private final RateLimitingFilter rateLimitingFilter;
    private final XSSFilter xssFilter;
//    private final ContentSecurityPolicyFilter contentSecurityPolicyFilter;
    private final RequestLoggingFilter requestLoggingFilter;
//    private final SecurityHeaderFilter securityHeaderFilter;
    private final RefreshTokenCookieFilter refreshTokenCookieFilter;


    private final  CorsConfigurationSource corsConfigurationSource;
    private final CustomAuthenticationFailureHandler customAuthenticationFailureHandler;
    private final org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;


    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http

        .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable())
                .headers(headers -> headers
                        // 1. Tắt hoàn toàn X-Frame-Options (Cái này gây ra lỗi Refused to display)
                        .frameOptions(frame -> frame.disable())
                        // 2. Cấu hình CSP để cho phép nhúng frame từ chính nó và Angular
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("frame-ancestors 'self' https://localhost:4200")
                        )
                )

                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                )

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login","/register","/resend-otp","/verify-otp", "/forgot-password" ,"/reset-password","/error", "/oauth2/**", "/.well-known/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll()

                        // Cho phép các API của mày (Resource Server mode)
                        .requestMatchers("/api/**").permitAll()
                        .requestMatchers("/ws/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**", "/v3/api-docs.yaml").permitAll()

                        .anyRequest().authenticated()
                )

                // Cấu hình Form Login
                .formLogin(form -> form
                                .loginPage("/login") // Mày dùng trang login tự chế hoặc mặc định
                                .loginProcessingUrl("/login")
                        .failureHandler(customAuthenticationFailureHandler)
                                .permitAll()

                )

                // Cấu hình để file này cũng hiểu JWT gửi lên các API
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(Customizer.withDefaults())
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new CustomAuthenticationEntryPoint())
                );
        http.addFilterBefore(refreshTokenCookieFilter, UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(requestLoggingFilter, UsernamePasswordAuthenticationFilter.class);

        // 2. Chặn Rate Limit ngay sau khi log (Tránh việc các request spam lọt vào trong)
        http.addFilterAfter(rateLimitingFilter, RequestLoggingFilter.class); // Hoặc addFilterBefore Username...

        // 3. Lọc XSS cho các Request Body/Param (Nếu Filter của bạn thiết kế theo chuẩn OncePerRequestFilter)
        http.addFilterAfter(xssFilter, RateLimitingFilter.class);


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
    @Bean
    public JwtEncoder jwtEncoder(JWKSource<SecurityContext> jwkSource) {
        return new NimbusJwtEncoder(jwkSource);
    }
    @Bean
    public OAuth2AuthorizationService authorizationService(JdbcTemplate jdbcTemplate, RegisteredClientRepository registeredClientRepository) {
        JdbcOAuth2AuthorizationService service = new JdbcOAuth2AuthorizationService(jdbcTemplate, registeredClientRepository);

        // 1. Khởi tạo ObjectMapper
        ObjectMapper objectMapper = new ObjectMapper();
        ClassLoader classLoader = JdbcOAuth2AuthorizationService.class.getClassLoader();

        // 2. Đăng ký các Module tiêu chuẩn của Security & OAuth2
        List<com.fasterxml.jackson.databind.Module> securityModules = SecurityJackson2Modules.getModules(classLoader);
        objectMapper.registerModules(securityModules);
        objectMapper.registerModule(new OAuth2AuthorizationServerJackson2Module());

        // 3. Đăng ký Mixin của bạn
        objectMapper.addMixIn(CustomUserDetails.class, CustomUserDetailsMixin.class);

        // 4. QUAN TRỌNG: Cấu hình cho phép các Class cơ bản của Java (như Long, Integer, List...)
        // Chúng ta sử dụng một whitelist nới lỏng cho các gói cơ bản
        objectMapper.activateDefaultTyping(
                objectMapper.getPolymorphicTypeValidator(),
                ObjectMapper.DefaultTyping.NON_FINAL,
                com.fasterxml.jackson.annotation.JsonTypeInfo.As.PROPERTY
        );

        // 5. Thiết lập RowMapper sử dụng ObjectMapper đã cấu hình
        JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper rowMapper =
                new JdbcOAuth2AuthorizationService.OAuth2AuthorizationRowMapper(registeredClientRepository);
        rowMapper.setObjectMapper(objectMapper);
        service.setAuthorizationRowMapper(rowMapper);

        // 6. Thiết lập ParametersMapper để khi LƯU vào DB cũng dùng chung format
        JdbcOAuth2AuthorizationService.OAuth2AuthorizationParametersMapper parametersMapper =
                new JdbcOAuth2AuthorizationService.OAuth2AuthorizationParametersMapper();
        parametersMapper.setObjectMapper(objectMapper);
        service.setAuthorizationParametersMapper(parametersMapper);

        return service;
    }
}