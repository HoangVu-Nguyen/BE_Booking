package clyvasync.Clyvasync.producer;

import clyvasync.Clyvasync.constant.MessagingConstants;
import clyvasync.Clyvasync.dto.event.PaymentRequestMailMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingProducer {
    private final RabbitTemplate rabbitTemplate;

    public void sendPaymentRequestMail(PaymentRequestMailMessage payload) {
        log.info("Đang đẩy Message yêu cầu thanh toán vào RabbitMQ cho Booking: {}", payload.getBookingCode());

        rabbitTemplate.convertAndSend(
                MessagingConstants.BOOKING_EXCHANGE,
                MessagingConstants.PAYMENT_MAIL_ROUTING_KEY,
                payload
        );
    }
}