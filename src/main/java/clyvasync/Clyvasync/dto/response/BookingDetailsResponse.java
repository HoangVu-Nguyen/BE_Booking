package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.dto.detail.PolicyDetail;
import clyvasync.Clyvasync.dto.detail.TourDetail;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingDetailsResponse {
    // 1. Thông tin chung đơn hàng
    private Long bookingId;
    private String bookingCode;
    private String status;
    private String paymentStatus;
    private String specialRequests;
    private Integer loyaltyPointsEarned;

    // 2. Chi tiết Homestay & Phòng đặt
    private Long homestayId;
    private String homestayName;
    private String homestayAddress;
    private String roomName;
    private String roomImage;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Long totalNights;
    private Integer roomQuantity;
    private Integer guestCount;

    private TourDetail tour;

    private BigDecimal roomSubtotal;
    private BigDecimal tourSubtotal;
    private BigDecimal taxFee;
    private BigDecimal grandTotal;
    private PolicyDetail policy;
}