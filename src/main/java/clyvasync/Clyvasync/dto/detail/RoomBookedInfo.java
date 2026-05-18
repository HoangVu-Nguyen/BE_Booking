package clyvasync.Clyvasync.dto.detail;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RoomBookedInfo {
    private String roomName; // Tên phòng (VD: Master Suite Villa)
    private String roomTag;  // Tag của phòng
    private int quantity;    // Số lượng phòng loại này
    private int guests;      // Số khách ở phòng này
}