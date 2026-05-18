package clyvasync.Clyvasync.dto.detail;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder
public class TourTimelineInfo {
    private String tourId;
    private String tourName;
    private String tourImage;
    private LocalDate tourDate;
    private String startTime; // Trả thẳng chuỗi "04:00" cho Angular đỡ phải DatePipe
    private String endTime;   // Trả thẳng chuỗi "09:00"
    private int participants;
}