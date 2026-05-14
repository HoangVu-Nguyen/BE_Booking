package clyvasync.Clyvasync.dto.request;

import lombok.Data;

import java.time.LocalDate;

@Data
public class BookingInitRequest {
    private Long homestayId;
    private Long roomId;
    private Long ratePlanId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private Integer roomQuantity;
    private Integer guestCount;

    // Thông tin Tour đi kèm (Có thể null nếu khách không chọn Tour)
    private Long tourId;
    private Long availabilityId;
    private LocalDate tourDate;
    private Integer participantCount;
    private String guestName;
    private String email;
    private String phone;
    private String specialRequests;
}