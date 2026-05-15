package clyvasync.Clyvasync.strategy;

import clyvasync.Clyvasync.enums.payment.PaymentMethod;
import clyvasync.Clyvasync.enums.type.PaymentStatus;
import clyvasync.Clyvasync.modules.booking.entity.Booking;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;

import java.util.Map;

public interface PaymentStrategy {
    String createPaymentUrl(Booking booking, HttpServletRequest request);

    PaymentStatus processCallback(Map<String, String> queryParams);

    PaymentMethod getMethod();
    String extractBookingCode(Map<String, String> params);
    ResponseEntity<?> buildIPNSuccessResponse(String message);
}