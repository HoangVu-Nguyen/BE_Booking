package clyvasync.Clyvasync.consumer;

import clyvasync.Clyvasync.constant.MessagingConstants;
import clyvasync.Clyvasync.dto.event.UserEventDTO;
import clyvasync.Clyvasync.dto.request.StateEmailRequest;
import clyvasync.Clyvasync.enums.mail.StateSendMail;
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
    public void handleRegisterEmail(UserEventDTO payload) {
        log.info("RabbitMQ Consumer: Nhận được yêu cầu gửi mail cho {}", payload.getEmail());

        // 1. Chuyển từ UserEventDTO (tin nhắn thô) sang StateEmailRequest (tin nhắn có nghiệp vụ)
        // Mặc định ở bước Register thì State là REGISTER
        StateEmailRequest emailRequest = new StateEmailRequest(
                payload.getEmail(),
                payload.getCode(),
                StateSendMail.REGISTER
        );

        // 2. Gọi MailService thực hiện render HTML và gửi mail
        try {
            mailService.sendStateEmail(emailRequest);
        } catch (Exception e) {
            log.error("Lỗi khi xử lý gửi mail từ Queue: {}", e.getMessage());
            // Nếu throw exception ở đây, RabbitMQ sẽ hiểu là xử lý thất bại và có thể Retry (nếu cấu hình)
            throw e;
        }
    }
}