package clyvasync.Clyvasync.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class RefreshTokenCookieFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Chỉ xử lý nếu là request đổi token (/oauth2/token) và trong Body đang thiếu refresh_token
        if (request.getRequestURI().contains("/oauth2/token") && request.getParameter("refresh_token") == null) {
            Cookie[] cookies = request.getCookies();
            if (cookies != null) {
                for (Cookie cookie : cookies) {
                    if ("refresh_token".equals(cookie.getName())) {
                        // "Lừa" Spring bằng cách tạo một Wrapper chứa giá trị token từ Cookie
                        HttpServletRequestWrapper wrapper = new HttpServletRequestWrapper(request) {
                            @Override
                            public String getParameter(String name) {
                                if ("refresh_token".equals(name)) return cookie.getValue();
                                return super.getParameter(name);
                            }
                        };
                        filterChain.doFilter(wrapper, response);
                        return;
                    }
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}