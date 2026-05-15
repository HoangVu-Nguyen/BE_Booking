package clyvasync.Clyvasync.dto.detail;

import lombok.Data;

import java.time.LocalDate;

@Data
public class TourBookingItemDetail {
    private Long tourId;
    private Long availabilityId;
    private LocalDate tourDate;
    private Integer participantCount;
}
