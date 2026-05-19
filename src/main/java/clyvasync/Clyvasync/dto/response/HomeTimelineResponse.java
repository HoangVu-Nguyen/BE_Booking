package clyvasync.Clyvasync.dto.response;

import lombok.Builder;

import java.util.List;
@Builder
public record HomeTimelineResponse(
        Long homeId,
        String homeName,
        String address,
        String primaryImageUrl,
        List<RoomTimelineResponse> rooms // Timeline của từng phòng trong Home này
) {}
