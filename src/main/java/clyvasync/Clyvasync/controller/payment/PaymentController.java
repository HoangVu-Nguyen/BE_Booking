package clyvasync.Clyvasync.controller.payment;

import clyvasync.Clyvasync.config.VnpayConfig;
import clyvasync.Clyvasync.enums.payment.PaymentMethod;
import clyvasync.Clyvasync.factory.PaymentFactory;
import clyvasync.Clyvasync.modules.booking.entity.Booking;
import clyvasync.Clyvasync.service.booking.BookingService;
import clyvasync.Clyvasync.strategy.PaymentStrategy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {


        private final PaymentFactory paymentFactory;
        private final BookingService bookingService;

        @GetMapping("/create-url")
        public ResponseEntity<String> createUrl(
                @RequestParam String bookingCode,
                @RequestParam PaymentMethod method,
                HttpServletRequest request) {

            Booking booking = bookingService.getBookingByCode(bookingCode);

            PaymentStrategy strategy = paymentFactory.getStrategy(method);

            String url = strategy.createPaymentUrl(booking, request);
            return ResponseEntity.ok(url);
        }
    }
