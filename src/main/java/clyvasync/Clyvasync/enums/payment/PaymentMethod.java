package clyvasync.Clyvasync.enums.payment;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentMethod {
    VNPAY("VNPAY", "Thanh toán qua cổng VNPAY"),
    MOMO("MOMO", "Thanh toán qua ví MoMo"),
    ZALOPAY("ZALOPAY", "Thanh toán qua ví ZaloPay"),
    BANK_TRANSFER("BANK_TRANSFER", "Chuyển khoản ngân hàng trực tiếp");

    private final String displayName;
    private final String description;
}