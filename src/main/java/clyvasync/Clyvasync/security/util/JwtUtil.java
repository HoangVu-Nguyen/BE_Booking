package clyvasync.Clyvasync.security.util;

import clyvasync.Clyvasync.config.JwtProperties;
import clyvasync.Clyvasync.modules.auth.entity.Role;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
public class JwtUtil {

    private final SecretKey key;
    private final JwtProperties jwtProperties;
    private static final String BEARER_PREFIX = "Bearer ";

    public JwtUtil(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        // Giải mã Secret Key từ Base64 (Cấu hình trong application.yml)
        byte[] keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
        this.key = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * Tạo Access Token (JWT)
     */
    public String generateAccessToken(String email, Set<Role> roles, Long userId) {
        // Chuyển Set<Role> thành List<String> có tiền tố ROLE_ để Spring Security hiểu
        List<String> roleNames = roles.stream()
                .map(role -> "ROLE_" + role.getName().name())
                .collect(Collectors.toList());

        return Jwts.builder()
                .subject(email)
                .claim("roles", roleNames)
                .claim("user_id", userId)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + jwtProperties.getAccessTokenExpiration()))
                .signWith(key)
                .compact();
    }

    /**
     * Giải mã và trích xuất thông tin từ Token
     */
    public Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token) // Phương thức mới của jjwt 0.12.x
                .getPayload();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object userId = extractAllClaims(token).get("user_id");
        if (userId instanceof Number) {
            return ((Number) userId).longValue();
        }
        return null;
    }

    /**
     * Lấy token từ Header "Authorization: Bearer <token>"
     */
    public String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    /**
     * Kiểm tra Token có hợp lệ/hết hạn hay không
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.error("JWT token hết hạn: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("JWT token sai định dạng: {}", e.getMessage());
        } catch (SignatureException e) {
            log.error("Chữ ký JWT không hợp lệ: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token không được hỗ trợ: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims trống: {}", e.getMessage());
        }
        return false;
    }
    public Date extractExpiration(String token) {
        return extractAllClaims(token).getExpiration();
    }
}