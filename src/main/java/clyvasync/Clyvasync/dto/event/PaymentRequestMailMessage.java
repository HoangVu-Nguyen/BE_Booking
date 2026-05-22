package clyvasync.Clyvasync.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequestMailMessage implements Serializable {
    private String bookingCode;
    private String guestName;
    private String guestEmail;
    private String homestayName;
    private String roomName;
    private BigDecimal grandTotal;

    private String checkoutUrl;
}