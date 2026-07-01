package clyvasync.Clyvasync.modules.room;

import clyvasync.Clyvasync.enums.room.BedType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface RoomBedRepositorys extends JpaRepository<RoomBed, Long> {

    // Lấy toàn bộ giường của 1 phòng
    List<RoomBed> findByRoomId(Long roomId);

    // Lấy giường theo loại (VD: khách lọc "chỉ muốn phòng có giường đôi")
    List<RoomBed> findByRoomIdAndBedType(Long roomId, BedType bedType);

    // Tổng số lượng giường của 1 phòng (dùng để hiển thị "phòng có X giường")
    @Query("SELECT COALESCE(SUM(rb.quantity), 0) FROM RoomBed rb WHERE rb.roomId = :roomId")
    Integer sumQuantityByRoomId(@Param("roomId") Long roomId);

    // Tìm các room_id có chứa loại giường cụ thể (phục vụ tính năng search/lọc phòng theo loại giường)
    @Query("SELECT DISTINCT rb.roomId FROM RoomBed rb WHERE rb.bedType = :bedType")
    List<Long> findRoomIdsByBedType(@Param("bedType") BedType bedType);

    // Kiểm tra 1 phòng có loại giường cụ thể hay không
    boolean existsByRoomIdAndBedType(Long roomId, BedType bedType);
}