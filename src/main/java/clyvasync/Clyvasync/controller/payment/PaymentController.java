package clyvasync.Clyvasync.controller.payment;

import clyvasync.Clyvasync.dto.response.ApiResponse;
import clyvasync.Clyvasync.enums.payment.PaymentMethod;
import clyvasync.Clyvasync.service.payment.PaymentService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
public class PaymentController {

    // 1. Chỉ inject độc nhất thằng PaymentService gánh team logic ở đây
    private final PaymentService paymentService;

    /**
     * API 1: Sinh link URL thanh toán (Đã chuyển qua gọi trung gian Service)
     */
    @GetMapping("/create-url")
    public ResponseEntity<String> createUrl(
            @RequestParam String bookingCode,
            @RequestParam PaymentMethod method,
            HttpServletRequest request) {

        String url = paymentService.createUrl(bookingCode, method, request);
        return ResponseEntity.ok(url);
    }

    /**
     * API 2: Nhận dữ liệu Return (Nổi) - Trả data phòng/tour sạch bọc ApiResponse về cho Angular dựng UI Luxury
     */
    @GetMapping("/{gateway}/return")
    public ApiResponse<Map<String, Object>> handlePaymentReturn(
            @PathVariable("gateway") String gateway,
            @RequestParam Map<String, String> params) {

        log.info("[CONTROLLER RETURN] Tiếp nhận data redirect từ gateway: {}", gateway);
        Map<String, Object> data = paymentService.processPaymentReturn(gateway, params);
        return ApiResponse.success(data);
    }

    /**
     * API 3: Webhook IPN (Chìm) - Server VNPAY/MoMo gọi ngầm chốt sổ database thực tế
     * KHÔNG bọc ApiResponse vì đối tác cần cấu trúc raw text/json riêng biệt để phản hồi kết quả (ví dụ RspCode: 00)
     */
    @RequestMapping(value = "/{gateway}/ipn", method = {RequestMethod.GET, RequestMethod.POST})
    public ResponseEntity<?> handlePaymentIPN(
            @PathVariable("gateway") String gateway,
            @RequestParam Map<String, String> params) {

        log.info("[CONTROLLER IPN] Hệ thống đối tác {} gọi Webhook cập nhật đơn hàng", gateway);
        return paymentService.processPaymentIPN(gateway, params);
    }
}