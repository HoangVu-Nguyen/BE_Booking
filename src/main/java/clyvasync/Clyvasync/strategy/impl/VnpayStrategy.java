package clyvasync.Clyvasync.strategy.impl;

import clyvasync.Clyvasync.enums.payment.PaymentMethod;
import clyvasync.Clyvasync.enums.type.PaymentStatus;
import clyvasync.Clyvasync.modules.booking.entity.Booking;
import clyvasync.Clyvasync.strategy.PaymentStrategy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
@Service
@RequiredArgsConstructor
public class VnpayStrategy implements PaymentStrategy {
    @Override
    public String createPaymentUrl(Booking booking, HttpServletRequest request) {
        return "";
    }

    @Override
    public PaymentStatus processCallback(Map<String, String> queryParams) {
        return null;
    }

    @Override
    public PaymentMethod getMethod() {
        return null;
    }
}
