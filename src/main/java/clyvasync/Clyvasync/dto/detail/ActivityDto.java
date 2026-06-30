package clyvasync.Clyvasync.dto.detail;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ActivityDto {
    private String id;
    private String title;
    private LocalDateTime time;
    private String type;
    private String status;
}