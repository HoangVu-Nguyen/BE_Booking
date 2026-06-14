package clyvasync.Clyvasync.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class MultiRoomBatchUploadRequest {
    private List<RoomImageBatch> rooms;
}
