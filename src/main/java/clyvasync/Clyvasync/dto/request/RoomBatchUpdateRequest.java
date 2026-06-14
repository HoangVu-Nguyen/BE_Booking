package clyvasync.Clyvasync.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RoomBatchUpdateRequest {
    private Long homestayId;
    private List<RoomUpdateRequest> rooms;
}