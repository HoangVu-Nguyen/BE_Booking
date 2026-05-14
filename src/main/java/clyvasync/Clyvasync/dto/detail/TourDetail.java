package clyvasync.Clyvasync.dto.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class TourDetail {
    private Long tourBookingId;
    private String tourBookingCode;
    private String tourName;
    private String tourImage;
    private LocalDate tourDate;
    private Integer participantCount;
}
