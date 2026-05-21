package clyvasync.Clyvasync.config;

import clyvasync.Clyvasync.constant.SocketDestinations; // <-- Import hằng số
import clyvasync.Clyvasync.interceptor.AuthChannelInterceptorAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final AuthChannelInterceptorAdapter authChannelInterceptorAdapter;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*"); // Ép nhận mọi Origin

        // (Tùy chọn) Bác có thể giữ thêm 1 dòng bọc SockJS phòng hờ test bằng tool khác
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        // Dùng hằng số để định nghĩa luồng Broker công khai và riêng tư
        registry.enableSimpleBroker(SocketDestinations.TOPIC_PREFIX.replace("/", ""), SocketDestinations.USER_QUEUE_PREFIX.replace("/", ""));
        registry.setApplicationDestinationPrefixes("/app");
        registry.setUserDestinationPrefix("/user");
    }
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(authChannelInterceptorAdapter);
    }
}