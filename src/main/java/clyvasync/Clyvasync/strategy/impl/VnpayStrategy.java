package clyvasync.Clyvasync.strategy.impl;

import clyvasync.Clyvasync.config.VnpayConfig;
import clyvasync.Clyvasync.enums.payment.PaymentMethod;
import clyvasync.Clyvasync.enums.type.PaymentStatus;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.booking.entity.Booking;
import clyvasync.Clyvasync.strategy.PaymentStrategy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class VnpayStrategy implements PaymentStrategy {

    private final VnpayConfig vnpayConfig;

    @Override
    public String createPaymentUrl(Booking booking, HttpServletRequest request) {
        long amount = booking.getTotalPrice().multiply(new BigDecimal("100")).longValue();
        String txnRef = booking.getBookingCode();

        String ipAddr = request.getHeader("X-Forwarded-For");
        if (ipAddr == null || ipAddr.isBlank()) {
            ipAddr = request.getRemoteAddr();
        }
        if ("0:0:0:0:0:0:0:1".equals(ipAddr)) {
            ipAddr = "127.0.0.1";
        }

        Map<String, String> vnpParams = new HashMap<>();
        vnpParams.put("vnp_Version", "2.1.0");
        vnpParams.put("vnp_Command", "pay");
        vnpParams.put("vnp_TmnCode", vnpayConfig.getVnp_TmnCode());
        vnpParams.put("vnp_Amount", String.valueOf(amount));
        vnpParams.put("vnp_CurrCode", "VND");
        vnpParams.put("vnp_TxnRef", txnRef);
        vnpParams.put("vnp_OrderInfo", "Thanh_toan_don_hang_" + txnRef);
        vnpParams.put("vnp_OrderType", "other");
        vnpParams.put("vnp_Locale", "vn");
        vnpParams.put("vnp_ReturnUrl", vnpayConfig.getVnp_ReturnUrl());
        vnpParams.put("vnp_IpAddr", ipAddr);

        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmmss");
        vnpParams.put("vnp_CreateDate", formatter.format(calendar.getTime()));

        calendar.add(Calendar.MINUTE, 15);
        vnpParams.put("vnp_ExpireDate", formatter.format(calendar.getTime()));

        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());
        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = vnpParams.get(fieldName);

            if (fieldValue != null && !fieldValue.isEmpty()) {
                String encodedValue = URLEncoder.encode(fieldValue, StandardCharsets.UTF_8).replace("+", "%20");
                hashData.append(fieldName).append("=").append(encodedValue);
                query.append(URLEncoder.encode(fieldName, StandardCharsets.UTF_8)).append("=").append(encodedValue);

                if (itr.hasNext()) {
                    hashData.append("&");
                    query.append("&");
                }
            }
        }

        String secureHash = vnpayConfig.hmacSHA512(hashData.toString());
        return vnpayConfig.getVnp_PayUrl() + "?" + query + "&vnp_SecureHash=" + secureHash;
    }

    @Override
    public PaymentStatus processCallback(Map<String, String> queryParams) {
        log.info("=========== VNPAY CALLBACK ===========");
        String vnpSecureHash = queryParams.get("vnp_SecureHash");

        // 1. Sắp xếp alphabet các key bắt đầu bằng vnp_
        List<String> fieldNames = new ArrayList<>(queryParams.keySet());
        Collections.sort(fieldNames);

        // 2. Dựng chuỗi ký thô (Raw Data) - TUYỆT ĐỐI KHÔNG ENCODE VALUE
        StringBuilder hashData = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = queryParams.get(fieldName);

            // Chỉ lấy các trường vnp_ và bỏ qua các trường chữ ký, trường rác trống
            if (fieldName != null && fieldName.startsWith("vnp_")
                    && !"vnp_SecureHash".equals(fieldName)
                    && !"vnp_SecureHashType".equals(fieldName)) {
                if (fieldValue != null && !fieldValue.isEmpty()) {

                    // Nối trực tiếp KEY = VALUE thô, KHÔNG dùng URLEncoder.encode ở đây!
                    hashData.append(fieldName).append("=").append(fieldValue);

                    if (itr.hasNext()) {
                        hashData.append("&");
                    }
                }
            }
        }

        // Xóa dấu & thừa ở cuối chuỗi nếu có do vòng lặp kết thúc sớm
        String rawData = hashData.toString();
        if (rawData.endsWith("&")) {
            rawData = rawData.substring(0, rawData.length() - 1);
        }

        // 3. Tự băm bằng SecretKey thông qua hàm hmacSHA512 có sẵn trong vnpayConfig của bác
        String calculatedHash = vnpayConfig.hmacSHA512(rawData);

        log.info("VNPAY RAW DATA STRING: {}", rawData);
        log.info("VNPAY HASH FROM GATEWAY: {}", vnpSecureHash);
        log.info("CALCULATED HASH IN SERVER: {}", calculatedHash);

        // 4. So khớp chữ ký số bảo mật
        if (!calculatedHash.equalsIgnoreCase(vnpSecureHash)) {
            log.error("INVALID VNPAY SIGNATURE - HASH MISMATCH!");
            throw new AppException(ResultCode.INVALID_PAYMENT_SIGNATURE);
        }

        // 5. Kiểm tra trạng thái đơn hàng hoàn tất xịn từ VNPAY
        if ("00".equals(queryParams.get("vnp_ResponseCode"))) {
            log.info("PAYMENT CONFIRMED SUCCESSFULLY!");
            return PaymentStatus.PAID;
        }
        return PaymentStatus.UNPAID;
    }

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.VNPAY;
    }

    @Override
    public String extractBookingCode(Map<String, String> params) {
        return params.get("vnp_TxnRef");
    }

    @Override
    public ResponseEntity<?> buildIPNSuccessResponse(String message) {
        // Chuẩn format JSON phản hồi bắt buộc của VNPAY danh cho IPN Webhook
        Map<String, String> response = new HashMap<>();
        response.put("RspCode", "00");
        response.put("Message", message);
        return ResponseEntity.ok(response);
    }
}