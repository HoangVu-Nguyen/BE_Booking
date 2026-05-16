package clyvasync.Clyvasync.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripTourResponse {
    private String tourId;
    private String tourName;
    private String tourImage;
    private int participants; // Số người tham gia tour này
    private LocalDate tourDate; // T7, 13 Thg 10
    private LocalTime startTime; // 04:00
    private LocalTime endTime;   // 09:00
}