package clyvasync.Clyvasync.service.mail.impl;

import clyvasync.Clyvasync.dto.event.PaymentRequestMailMessage;
import clyvasync.Clyvasync.dto.request.StateEmailRequest;
import clyvasync.Clyvasync.service.mail.MailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context; // QUAN TRỌNG: Phải có import này

import java.nio.charset.StandardCharsets;
import java.text.NumberFormat;
import java.util.Locale;

@Service
@Slf4j
@RequiredArgsConstructor
public class MailServiceImpl implements MailService {

    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Override
    public void sendStateEmail(StateEmailRequest request) {
        try {
            log.info("Đang chuẩn bị gửi email [{}] tới: {}", request.getState().getTitle(), request.getEmail());

            MimeMessage message = mailSender.createMimeMessage();
            // Sử dụng UTF-8 để không bị lỗi font tiếng Việt
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            // 1. Đổ dữ liệu vào context của Thymeleaf
            Context context = new Context();
            context.setVariable("email", request.getEmail());
            context.setVariable("code", request.getCode());
            context.setVariable("title", request.getState().getTitle());
            context.setVariable("intro", request.getState().getIntro());

            // 2. Render ra file HTML (File này nằm ở: src/main/resources/templates/email/state-mail.html)
            String htmlContent = templateEngine.process("email/state-mail", context);

            // 3. Thiết lập thông tin người nhận và nội dung
            helper.setTo(request.getEmail());
            helper.setSubject("Clyvasync - " + request.getState().getTitle());
            helper.setText(htmlContent, true); // true để gửi định dạng HTML
            helper.setFrom("Clyvasync Support <no-reply@clyvasync.com>");

            // 4. Bắn mail đi
            mailSender.send(message);
            log.info("Email [{}] đã gửi thành công tới {}", request.getState().getTitle(), request.getEmail());

        } catch (MessagingException e) {
            log.error("Lỗi MessagingException khi gửi mail tới {}: {}", request.getEmail(), e.getMessage());
            throw new RuntimeException("Gửi mail thất bại do lỗi hệ thống thư thoại", e);
        } catch (Exception e) {
            log.error("Lỗi không xác định khi gửi mail: {}", e.getMessage());
            throw new RuntimeException("Gửi mail thất bại", e);
        }
    }

    @Override
    public void sendPaymentRequestEmail(PaymentRequestMailMessage msg) {
        try {
            log.info("Đang chuẩn bị gửi email yêu cầu thanh toán tới: {}", msg.getGuestEmail());

            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, StandardCharsets.UTF_8.name());

            // 1. Format tiền tệ (VND)
            Locale vietnam = new Locale("vi", "VN");
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(vietnam);
            String formattedPrice = currencyFormat.format(msg.getGrandTotal());

            // 2. Đổ dữ liệu vào context của Thymeleaf
            Context context = new Context();
            context.setVariable("guestName", msg.getGuestName());
            context.setVariable("bookingCode", msg.getBookingCode());
            context.setVariable("homestayName", msg.getHomestayName());
            context.setVariable("roomName", msg.getRoomName());
            context.setVariable("grandTotal", formattedPrice);
            context.setVariable("checkoutUrl", msg.getCheckoutUrl());

            // 3. Render ra file HTML (Tạo file payment-request.html ở src/main/resources/templates/email/)
            String htmlContent = templateEngine.process("email/payment-request", context);

            // 4. Thiết lập thông tin gửi
            helper.setTo(msg.getGuestEmail());
            helper.setSubject("Clyvasync - Yêu cầu thanh toán đơn #" + msg.getBookingCode());
            helper.setText(htmlContent, true);
            helper.setFrom("Clyvasync Support <no-reply@clyvasync.com>");

            // 5. Bắn mail
            mailSender.send(message);
            log.info("Email yêu cầu thanh toán đã gửi thành công tới: {}", msg.getGuestEmail());

        } catch (MessagingException e) {
            log.error("Lỗi MessagingException khi gửi mail thanh toán tới {}: {}", msg.getGuestEmail(), e.getMessage());
            throw new RuntimeException("Gửi mail thanh toán thất bại do lỗi hệ thống thư thoại", e);
        } catch (Exception e) {
            log.error("Lỗi không xác định khi gửi mail thanh toán: {}", e.getMessage());
            throw new RuntimeException("Gửi mail thanh toán thất bại", e);
        }
    }
}