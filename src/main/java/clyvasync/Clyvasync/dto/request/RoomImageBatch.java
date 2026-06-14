package clyvasync.Clyvasync.dto.request;

import lombok.Data;

import java.util.List;

@Data
public class RoomImageBatch {
    private Long roomId;
    private List<UploadRequest> items; // Danh sách ảnh PENDING của phòng này
}
