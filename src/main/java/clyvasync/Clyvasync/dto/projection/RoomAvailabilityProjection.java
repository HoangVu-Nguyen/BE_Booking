package clyvasync.Clyvasync.dto.projection;

import clyvasync.Clyvasync.modules.homestay.entity.HomestayRoom;

public interface RoomAvailabilityProjection {
    // Chỉ lấy ID thay vì cả Object
    Long getId();

    // Lấy cột tính toán
    Integer getAvailableQty();
}