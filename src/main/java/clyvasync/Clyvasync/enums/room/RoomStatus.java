package clyvasync.Clyvasync.enums.room;

public enum RoomStatus {
    ACTIVE,      // Phòng đang mở bán bình thường
    INACTIVE,    // Phòng tạm đóng cửa (không cho hiện lên web)
    BLOCKED,
    MAINTENANCE  // Phòng đang sửa chữa (vẫn hiện nhưng không cho đặt)
}