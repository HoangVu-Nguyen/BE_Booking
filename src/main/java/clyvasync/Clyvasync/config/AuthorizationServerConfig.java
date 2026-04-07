package clyvasync.Clyvasync.config;

import clyvasync.Clyvasync.security.custom.CustomUserDetails;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.proc.SecurityContext;
import jakarta.servlet.http.Cookie;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.AuthorizationGrantType;
import org.springframework.security.oauth2.core.ClientAuthenticationMethod;
import org.springframework.security.oauth2.core.endpoint.OAuth2AccessTokenResponse;
import org.springframework.security.oauth2.core.http.converter.OAuth2AccessTokenResponseHttpMessageConverter;
import org.springframework.security.oauth2.core.oidc.OidcScopes;
import org.springframework.security.oauth2.server.authorization.authentication.OAuth2AccessTokenAuthenticationToken;
import org.springframework.security.oauth2.server.authorization.client.InMemoryRegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClient;
import org.springframework.security.oauth2.server.authorization.client.RegisteredClientRepository;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configuration.OAuth2AuthorizationServerConfiguration;
import org.springframework.security.oauth2.server.authorization.config.annotation.web.configurers.OAuth2AuthorizationServerConfigurer;
import org.springframework.security.oauth2.server.authorization.settings.AuthorizationServerSettings;
import org.springframework.security.oauth2.server.authorization.settings.ClientSettings;
import org.springframework.security.oauth2.server.authorization.settings.TokenSettings;
import org.springframework.security.oauth2.server.authorization.token.JwtEncodingContext;
import org.springframework.security.oauth2.server.authorization.token.OAuth2TokenCustomizer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.util.matcher.MediaTypeRequestMatcher;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class AuthorizationServerConfig {

    private final PasswordEncoder passwordEncoder;

    /**
     * CHAIN 1: Giao thức OAuth2
     * Xử lý các endpoint: /.well-known/openid-configuration, /oauth2/authorize, /oauth2/token, /oauth2/jwks
     */
    @Bean
    @Order(1)
    public SecurityFilterChain authServerSecurityFilterChain(HttpSecurity http) throws Exception {
        OAuth2AuthorizationServerConfiguration.applyDefaultSecurity(http);

        http.getConfigurer(OAuth2AuthorizationServerConfigurer.class)
                .oidc(Customizer.withDefaults())
                .tokenEndpoint(tokenEndpoint ->
                        tokenEndpoint
                                .accessTokenResponseHandler((request, response, authentication) -> {

                                    if (authentication instanceof OAuth2AccessTokenAuthenticationToken tokenAuth) {

                                        var accessToken = tokenAuth.getAccessToken();
                                        var refreshToken = tokenAuth.getRefreshToken();

                                        // 1️⃣ Set refresh token vào HttpOnly Cookie
                                        // Trong accessTokenResponseHandler
                                        if (refreshToken != null) {
                                            // Sử dụng Lax để trình duyệt dễ chấp nhận hơn ở môi trường local
                                            // Nếu FE là 4200 và BE là 8443, dùng Lax vẫn chạy tốt vì chúng cùng là 'localhost'
                                            String cookieValue = "refresh_token=" + refreshToken.getTokenValue()
                                                    + "; HttpOnly"
                                                    + "; Secure" // Vẫn để Secure vì bạn dùng HTTPS
                                                    + "; Path=/"
                                                    + "; Max-Age=" + (60 * 60 * 24 * 30)
                                                    + "; SameSite=Lax"; // Đổi None -> Lax

                                            response.addHeader("Set-Cookie", cookieValue);
                                            System.out.println("DEBUG: Đã ghi Set-Cookie vào Header thành công");
                                        }

                                        // 2️⃣ Trả access token về JSON cho FE
                                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

                                        String json = """
                    {
                        "access_token": "%s",
                        "token_type": "Bearer",
                        "expires_in": %d
                    }
                    """.formatted(
                                                accessToken.getTokenValue(),
                                                accessToken.getExpiresAt().getEpochSecond()
                                                        - accessToken.getIssuedAt().getEpochSecond()
                                        );

                                        response.getWriter().write(json);
                                    }
                                })
                );

        http
                .exceptionHandling((exceptions) -> exceptions
                        .defaultAuthenticationEntryPointFor(
                                new LoginUrlAuthenticationEntryPoint("/login"),
                                new MediaTypeRequestMatcher(MediaType.TEXT_HTML)
                        )
                )

                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()));

        return http.build();
    }
    /**
     * CHAIN 2: Xác thực người dùng (Form Login)
     * Xử lý việc hiển thị Form Login và lưu trữ Session để tiếp tục luồng OAuth2.
     */


    @Bean
    public RegisteredClientRepository registeredClientRepository() {
        RegisteredClient oidcClient = RegisteredClient.withId(UUID.randomUUID().toString())
                .clientId("my-client-frontend")
                .clientSecret(passwordEncoder.encode("secret-khong-ma-hoa"))
                .clientAuthenticationMethod(ClientAuthenticationMethod.NONE)

                // Đảm bảo chỉ khai báo Grant Type một lần cho rõ ràng
                .authorizationGrantType(AuthorizationGrantType.AUTHORIZATION_CODE)
                .authorizationGrantType(AuthorizationGrantType.REFRESH_TOKEN)

                .redirectUri("https://localhost:4200/callback")
                .redirectUri("https://localhost:4200/assets/silent-refresh.html")
                .postLogoutRedirectUri("https://localhost:4200/callback")

                .scope(OidcScopes.OPENID)
                .scope(OidcScopes.PROFILE)
                .scope(OidcScopes.EMAIL)
                .scope("offline_access") // Scope quan trọng để lấy Refresh Token

                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(false)
                        .build())
                .clientSettings(ClientSettings.builder()
                        .requireProofKey(true)
                        .requireAuthorizationConsent(false)
                        .build())

                // CHỈ GỌI tokenSettings MỘT LẦN DUY NHẤT
                .tokenSettings(TokenSettings.builder()
                        .accessTokenTimeToLive(Duration.ofMinutes(60)) // Access token 1 tiếng
                        .refreshTokenTimeToLive(Duration.ofDays(30))   // Refresh token 30 ngày
                        .reuseRefreshTokens(true)                      // Cho phép dùng lại mã cũ hoặc false nếu muốn rotation
                        .build())
                .build();

        return new InMemoryRegisteredClientRepository(oidcClient);
    }
    /**
     * Đưa thêm ID và Role vào JWT Token
     */
    @Bean
    public OAuth2TokenCustomizer<JwtEncodingContext> tokenCustomizer() {
        return context -> {
            Authentication auth = context.getPrincipal();

            // Nhồi Roles vào Access Token
            Set<String> authorities = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .collect(Collectors.toSet());
            context.getClaims().claim("roles", authorities);

            // Nhồi ID người dùng từ CustomUserDetails vào Access Token
            Object principal = auth.getPrincipal();
            if (principal instanceof CustomUserDetails userDetails) {
                context.getClaims().claim("user_id", userDetails.getId());
                context.getClaims().claim("email", userDetails.getEmail());
            } else {
                context.getClaims().claim("user_id", auth.getName());
            }
        };
    }

    @Bean
    public AuthorizationServerSettings authorizationServerSettings() {
        return AuthorizationServerSettings.builder()
                .issuer("https://localhost:8443")
                .build();
    }

    @Bean
    public JWKSource<SecurityContext> jwkSource() {
        KeyPair keyPair = generateRsaKey();
        RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
        RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
        RSAKey rsaKey = new RSAKey.Builder(publicKey)
                .privateKey(privateKey)
                .keyID(UUID.randomUUID().toString())
                .build();
        JWKSet jwkSet = new JWKSet(rsaKey);
        return new ImmutableJWKSet<>(jwkSet);
    }

    private static KeyPair generateRsaKey() {
        KeyPair keyPair;
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
            keyPairGenerator.initialize(2048);
            keyPair = keyPairGenerator.generateKeyPair();
        } catch (Exception ex) {
            throw new IllegalStateException(ex);
        }
        return keyPair;
    }
}