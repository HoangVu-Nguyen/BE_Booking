package clyvasync.Clyvasync.dto.response;

import lombok.Builder;
import java.time.LocalDate;
import java.util.List;

@Builder
public record HomestayTimelineResponse(
        Long homestayId,
        LocalDate startDate,
        LocalDate endDate,
        List<RoomTimelineResponse> rooms
) {}