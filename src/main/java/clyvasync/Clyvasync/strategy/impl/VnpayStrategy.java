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

        long amount = booking.getTotalPrice()
                .multiply(new BigDecimal("100"))
                .longValue();

        String txnRef = booking.getBookingCode();

        /*
         * Lấy IP thật khi chạy qua nginx/docker
         */
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

        vnpParams.put(
                "vnp_OrderInfo",
                "Thanh_toan_don_hang_" + txnRef
        );

        vnpParams.put("vnp_OrderType", "other");

        vnpParams.put("vnp_Locale", "vn");

        vnpParams.put("vnp_ReturnUrl", vnpayConfig.getVnp_ReturnUrl());

        vnpParams.put("vnp_IpAddr", ipAddr);

        /*
         * Timezone VN
         */
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("Asia/Ho_Chi_Minh"));

        SimpleDateFormat formatter =
                new SimpleDateFormat("yyyyMMddHHmmss");

        String createDate = formatter.format(calendar.getTime());

        vnpParams.put("vnp_CreateDate", createDate);

        /*
         * Expire sau 15 phút
         */
        calendar.add(Calendar.MINUTE, 15);

        String expireDate = formatter.format(calendar.getTime());

        vnpParams.put("vnp_ExpireDate", expireDate);

        /*
         * Sort alphabet
         */
        List<String> fieldNames = new ArrayList<>(vnpParams.keySet());

        Collections.sort(fieldNames);

        StringBuilder hashData = new StringBuilder();

        StringBuilder query = new StringBuilder();

        Iterator<String> itr = fieldNames.iterator();

        while (itr.hasNext()) {

            String fieldName = itr.next();

            String fieldValue = vnpParams.get(fieldName);

            if (fieldValue != null && !fieldValue.isEmpty()) {

                String encodedValue =
                        URLEncoder.encode(fieldValue, StandardCharsets.UTF_8)
                                .replace("+", "%20");

                /*
                 * HASH DATA
                 */
                hashData.append(fieldName)
                        .append("=")
                        .append(encodedValue);

                /*
                 * QUERY
                 */
                query.append(
                                URLEncoder.encode(fieldName, StandardCharsets.UTF_8)
                        )
                        .append("=")
                        .append(encodedValue);

                if (itr.hasNext()) {
                    hashData.append("&");
                    query.append("&");
                }
            }
        }

        String secureHash =
                vnpayConfig.hmacSHA512(hashData.toString());

        String paymentUrl =
                vnpayConfig.getVnp_PayUrl()
                        + "?"
                        + query
                        + "&vnp_SecureHash="
                        + secureHash;

        log.info("=========== VNPAY CREATE URL ===========");
        log.info("Hash Data: {}", hashData);
        log.info("Secure Hash: {}", secureHash);
        log.info("Payment URL: {}", paymentUrl);

        return paymentUrl;
    }

    @Override
    public PaymentStatus processCallback(Map<String, String> queryParams) {

        log.info("=========== VNPAY CALLBACK ===========");
        log.info("Raw Params: {}", queryParams);

        String vnpSecureHash =
                queryParams.get("vnp_SecureHash");

        Map<String, String> fields =
                new HashMap<>(queryParams);

        fields.remove("vnp_SecureHash");
        fields.remove("vnp_SecureHashType");

        String calculatedHash =
                vnpayConfig.hashAllFields(fields);

        log.info("VNPAY HASH: {}", vnpSecureHash);
        log.info("CALCULATED HASH: {}", calculatedHash);

        if (!calculatedHash.equals(vnpSecureHash)) {

            log.error("INVALID VNPAY SIGNATURE");

            throw new AppException(ResultCode.INVALID_PAYMENT_SIGNATURE);
        }

        String responseCode =
                queryParams.get("vnp_ResponseCode");

        /*
         * 00 = SUCCESS
         */
        if ("00".equals(responseCode)) {

            log.info("PAYMENT SUCCESS");

            return PaymentStatus.PAID;
        }

        log.warn("PAYMENT FAILED - RESPONSE CODE: {}", responseCode);

        return PaymentStatus.UNPAID;
    }

    @Override
    public PaymentMethod getMethod() {
        return PaymentMethod.VNPAY;
    }
}