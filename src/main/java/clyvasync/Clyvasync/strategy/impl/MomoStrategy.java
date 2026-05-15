package clyvasync.Clyvasync.strategy.impl;

import clyvasync.Clyvasync.config.MomoConfig;
import clyvasync.Clyvasync.enums.payment.PaymentMethod;
import clyvasync.Clyvasync.enums.type.PaymentStatus;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.booking.entity.Booking;
import clyvasync.Clyvasync.strategy.PaymentStrategy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class MomoStrategy implements PaymentStrategy {

    private final MomoConfig momoConfig;
    private final RestTemplate restTemplate = new RestTemplate();

    @Override
    public String createPaymentUrl(Booking booking, HttpServletRequest request) {
        try {
            // 1. Khởi tạo tham số
            // Khác với VNPAY, MoMo giữ nguyên giá trị VND, không nhân 100
            String amount = String.valueOf(booking.getTotalPrice().longValue());
            String orderId = booking.getBookingCode();
            String requestId = orderId + "_" + System.currentTimeMillis(); // RequestId nên là duy nhất mỗi lần gọi
            String orderInfo = "Thanh toan don hang Clyvasync: " + orderId;
            String requestType = "captureWallet";
            String extraData = ""; // Có thể pass Base64 JSON data nếu cần

            // 2. Build chuỗi dữ liệu thô (Raw Data) theo đúng thứ tự MoMo yêu cầu
            String rawSignature = "accessKey=" + momoConfig.getAccessKey() +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&ipnUrl=" + momoConfig.getIpnUrl() +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&partnerCode=" + momoConfig.getPartnerCode() +
                    "&redirectUrl=" + momoConfig.getRedirectUrl() +
                    "&requestId=" + requestId +
                    "&requestType=" + requestType;

            // 3. Băm chữ ký bằng HMAC-SHA256
            String signature = hmacSHA256(rawSignature, momoConfig.getSecretKey());

            // 4. Tạo Body JSON để gửi Request
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("partnerCode", momoConfig.getPartnerCode());
            requestBody.put("partnerName", "Clyvasync");
            requestBody.put("storeId", "ClyvasyncStore");
            requestBody.put("requestId", requestId);
            requestBody.put("amount", amount);
            requestBody.put("orderId", orderId);
            requestBody.put("orderInfo", orderInfo);
            requestBody.put("redirectUrl", momoConfig.getRedirectUrl());
            requestBody.put("ipnUrl", momoConfig.getIpnUrl());
            requestBody.put("lang", "vi");
            requestBody.put("extraData", extraData);
            requestBody.put("requestType", requestType);
            requestBody.put("signature", signature);

            // 5. Cấu hình Header và gọi API sang MoMo
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(
                    momoConfig.getEndpoint(), // Ví dụ: https://test-payment.momo.vn/v2/gateway/api/create
                    entity,
                    Map.class
            );

            // 6. Xử lý phản hồi từ MoMo
            Map<String, Object> responseBody = response.getBody();
            if (responseBody != null && responseBody.containsKey("payUrl")) {
                // Thành công, trả về URL để FE mở ra cho khách quét mã
                return (String) responseBody.get("payUrl");
            } else {
                log.error("[MOMO Create] Lỗi từ MoMo: {}", responseBody);
                throw new AppException(ResultCode.PAYMENT_CREATION_FAILED);
            }
        } catch (Exception e) {
            log.error("[MOMO Create] Ngoại lệ khi tạo URL MoMo", e);
            throw new AppException(ResultCode.PAYMENT_CREATION_FAILED);
        }
    }

    @Override
    public PaymentStatus processCallback(Map<String, String> queryParams) {
        log.info("[MOMO Callback] Bắt đầu xử lý IPN/Return Data: {}", queryParams);

        try {
            // 1. Lấy dữ liệu từ MoMo đẩy về
            String partnerCode = queryParams.get("partnerCode");
            String orderId = queryParams.get("orderId");
            String requestId = queryParams.get("requestId");
            String amount = queryParams.get("amount");
            String orderInfo = queryParams.get("orderInfo");
            String orderType = queryParams.get("orderType");
            String transId = queryParams.get("transId");
            String resultCode = queryParams.get("resultCode");
            String message = queryParams.get("message");
            String payType = queryParams.get("payType");
            String responseTime = queryParams.get("responseTime");
            String extraData = queryParams.get("extraData");
            String providedSignature = queryParams.get("signature");

            // 2. Build lại Raw Data để verify chữ ký (Đúng thứ tự tài liệu MoMo)
            String rawSignature = "accessKey=" + momoConfig.getAccessKey() +
                    "&amount=" + amount +
                    "&extraData=" + extraData +
                    "&message=" + message +
                    "&orderId=" + orderId +
                    "&orderInfo=" + orderInfo +
                    "&orderType=" + orderType +
                    "&partnerCode=" + partnerCode +
                    "&payType=" + payType +
                    "&requestId=" + requestId +
                    "&responseTime=" + responseTime +
                    "&resultCode=" + resultCode +
                    "&transId=" + transId;

            // 3. Tự băm lại
            String calculatedSignature = hmacSHA256(rawSignature, momoConfig.getSecretKey());

            // 4. So sánh chữ ký
            if (calculatedSignature.equals(providedSignature)) {
                // MoMo quy định resultCode = "0" là thành công
                if ("0".equals(resultCode)) {
                    log.info("[MOMO Callback] Giao dịch THÀNH CÔNG cho mã: {}", orderId);
                    return PaymentStatus.PAID;
                } else {
                    log.warn("[MOMO Callback] Giao dịch THẤT BẠI cho mã: {}. Lỗi: {}", orderId, message);
                    return PaymentStatus.UNPAID; // Hoặc FAILED
                }
            } else {
                log.error("[MOMO Callback] Sai lệch chữ ký (Invalid Signature) cho mã: {}", orderId);
                throw new AppException(ResultCode.INVALID_PAYMENT_SIGNATURE);
            }
        } catch (Exception e) {
            log.error("[MOMO Callback] Lỗi khi xử lý Callback MoMo", e);
            throw new AppException(ResultCode.INVALID_PAYMENT_SIGNATURE);
        }
    }

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.MOMO;
    }

    @Override
    public String extractBookingCode(Map<String, String> params) {
        return "";
    }

    @Override
    public ResponseEntity<?> buildIPNSuccessResponse(String message) {
        return null;
    }

    private String hmacSHA256(String data, String secretKey) throws Exception {
        Mac hmac256 = Mac.getInstance("HmacSHA256");
        SecretKeySpec secretKeySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        hmac256.init(secretKeySpec);
        byte[] hash = hmac256.doFinal(data.getBytes(StandardCharsets.UTF_8));
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}