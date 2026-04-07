package clyvasync.Clyvasync.consumer;

import clyvasync.Clyvasync.constant.MessagingConstants;
import clyvasync.Clyvasync.dto.event.UserEventDTO;
import clyvasync.Clyvasync.dto.request.StateEmailRequest;
import clyvasync.Clyvasync.enums.mail.StateSendMail;
import clyvasync.Clyvasync.enums.otp.OtpType;
import clyvasync.Clyvasync.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class EmailConsumer {
    private final MailService mailService;

    @RabbitListener(queues = MessagingConstants.EMAIL_QUEUE)
    public void handleEmailEvent(UserEventDTO payload) { // Đổi tên hàm cho tổng quát
        log.info("RabbitMQ Consumer: Nhận được yêu cầu gửi mail [{}] cho {}", payload.getType(), payload.getEmail());

        // 1. Ánh xạ từ type (String/Enum trong DTO) sang StateSendMail
        StateSendMail state = determineEmailState(payload.getType());

        // 2. Chuyển từ UserEventDTO sang StateEmailRequest
        StateEmailRequest emailRequest = new StateEmailRequest(
                payload.getEmail(),
                payload.getCode(),
                state
        );

        // 3. Gọi MailService thực hiện render HTML và gửi mail
        try {
            mailService.sendStateEmail(emailRequest);
            log.info("Đã gửi mail {} thành công cho: {}", state.name(), payload.getEmail());
        } catch (Exception e) {
            log.error("Lỗi khi xử lý gửi mail từ Queue cho {}: {}", payload.getEmail(), e.getMessage());
            // Throw exception để RabbitMQ hiểu là thất bại -> Tự động đưa vào hàng chờ Retry (nếu có cấu hình)
            throw e;
        }
    }

    /**
     * Hàm phụ trợ ánh xạ OtpType sang StateSendMail
     */
    private StateSendMail determineEmailState(String typeString) {
        // Nếu payload chưa có type (phiên bản cũ) thì mặc định là REGISTER
        if (typeString == null || typeString.trim().isEmpty()) {
            return StateSendMail.REGISTER;
        }

        try {
            OtpType type = OtpType.valueOf(typeString.toUpperCase());

            // Rẽ nhánh trạng thái Mail tùy theo loại OTP
            return switch (type) {
                case RECOVERY -> StateSendMail.FORGOT_PASSWORD; // Tên enum bên MailService của bạn
                case ACTIVATION -> StateSendMail.REGISTER;
                // Thêm các case khác sau này nếu có (như Đổi email, Xác thực GD...)
            };
        } catch (IllegalArgumentException e) {
            log.warn("Không nhận diện được type: {}, dùng mặc định REGISTER", typeString);
            return StateSendMail.REGISTER;
        }
    }
}