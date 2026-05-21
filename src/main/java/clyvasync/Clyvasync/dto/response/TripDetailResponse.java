package clyvasync.Clyvasync.dto.response;


import clyvasync.Clyvasync.dto.detail.PropertyDetailInfo;
import clyvasync.Clyvasync.dto.detail.RoomBookedInfo;
import clyvasync.Clyvasync.dto.detail.TourTimelineInfo;
import clyvasync.Clyvasync.enums.type.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripDetailResponse {
    private String bookingCode;
    private String status;
    private PaymentStatus paymentStatus;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int totalGuests;
    private BigDecimal totalPrice;
    private String paymentMethod; // Bác có thể giả lập hoặc map từ bảng thanh toán (VD: VISA **** 4242)

    private PropertyDetailInfo property;

    // 3. Thông tin Quản gia / Host
    private OwnerResponse host;

    private List<RoomBookedInfo> rooms;

    private List<TourTimelineInfo> tours;
    private BookingPolicyResponse policy;
}