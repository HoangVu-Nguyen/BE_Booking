package clyvasync.Clyvasync.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
public class HostBookingItemResponse {
    private String bookingCode;
    private String guestName;
    private String guestPhone;
    private String guestEmail;
    private String guestAvatar;

    private String homestayName;
    private String roomName;

    private int adults;
    private int children;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime checkInDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime checkOutDate;
    private long nights;

    private String source; // VD: "Website Trực tiếp"
    private BigDecimal totalPrice;
    private BigDecimal paidAmount;

    private String status; // "CONFIRMED", "PENDING", "CANCELLED"
}
