package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.enums.payment.PaymentMethodStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserPaymentMethodResponse {
    private Long id;
    private Long userId;
    private String provider;       // 'STRIPE'
    private String cardBrand;      // 'VISA', 'MASTERCARD'
    private String cardType;       // 'CREDIT', 'DEBIT'
    private String lastFour;       // '4242'
    private Integer expMonth;      // 12
    private Integer expYear;       // 2026
    private String cardHolderName; // 'VU NGUYEN'
    private Boolean isPrimary;     // true/false
    private PaymentMethodStatus status; // Enum ACTIVE, EXPIRED, LOCKED
}