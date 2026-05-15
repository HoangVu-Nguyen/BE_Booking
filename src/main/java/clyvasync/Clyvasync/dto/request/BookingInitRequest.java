package clyvasync.Clyvasync.dto.request;

import clyvasync.Clyvasync.dto.detail.TourBookingItemDetail;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class BookingInitRequest {
    private Long homestayId;
    private Long roomId;
    private Long ratePlanId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer roomQuantity;
    private Integer guestCount;

    private List<TourBookingItemDetail> tours;
    private String guestName;
    private String email;
    private String phone;
    private String specialRequests;
}