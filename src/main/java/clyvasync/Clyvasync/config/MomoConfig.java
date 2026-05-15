package clyvasync.Clyvasync.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Getter
public class MomoConfig {

    @Value("${momo.partner-code}")
    private String partnerCode;

    @Value("${momo.access-key}")
    private String accessKey;

    @Value("${momo.secret-key}")
    private String secretKey;

    // Đổi tên biến map cho khớp với chuỗi momo.pay-url trong properties
    @Value("${momo.pay-url}")
    private String endpoint;

    // Đổi tên biến map cho khớp với chuỗi momo.return-url trong properties
    @Value("${momo.return-url}")
    private String redirectUrl;

    @Value("${momo.ipn-url}")
    private String ipnUrl;
}