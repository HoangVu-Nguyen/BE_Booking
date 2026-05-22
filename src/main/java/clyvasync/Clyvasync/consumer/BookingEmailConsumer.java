package clyvasync.Clyvasync.consumer;

import clyvasync.Clyvasync.constant.MessagingConstants;
import clyvasync.Clyvasync.dto.event.PaymentRequestMailMessage;
import clyvasync.Clyvasync.service.mail.MailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component

public class BookingEmailConsumer {
    private final MailService mailService;
    @RabbitListener(queues = MessagingConstants.PAYMENT_MAIL_QUEUE)
    public void handlePaymentRequestMail(PaymentRequestMailMessage payload) {
        log.info("RabbitMQ Consumer: Nhận yêu cầu gửi mail thanh toán cho Booking [{}]", payload.getBookingCode());

        try {
            // Thay vì tự build HTML như code cũ, tôi giả sử bác sẽ viết thêm
            // một hàm "sendPaymentRequestEmail" vào trong MailService cho đồng bộ.
            mailService.sendPaymentRequestEmail(payload);

            log.info("Đã gửi mail yêu cầu thanh toán thành công tới: {}", payload.getGuestEmail());
        } catch (Exception e) {
            log.error("Lỗi gửi mail thanh toán cho Booking [{}]: {}", payload.getBookingCode(), e.getMessage());
            // Quăng exception để RabbitMQ bắt và Retry (DLQ)
            throw new RuntimeException("Gửi email thanh toán thất bại", e);
        }
    }
}
