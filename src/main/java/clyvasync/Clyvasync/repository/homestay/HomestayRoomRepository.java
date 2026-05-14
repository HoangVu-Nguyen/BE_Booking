package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.dto.projection.RoomAvailabilityProjection;
import clyvasync.Clyvasync.dto.summary.HomestayRoomSummary;
import clyvasync.Clyvasync.enums.room.RoomStatus;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HomestayRoomRepository extends JpaRepository<HomestayRoom,Long> {
    List<HomestayRoom> findAllByHomestayId(Long homestayId);

    @Query("""
        SELECT new clyvasync.Clyvasync.dto.summary.HomestayRoomSummary(
            r.homestayId,
            MIN(rp.price),
            MAX(r.maxGuests),
            CAST(COUNT(r) AS int)
        )
        FROM HomestayRoom r
        JOIN RoomRatePlan rp ON rp.roomId = r.id
        WHERE r.homestayId IN :homestayIds AND r.status = 'ACTIVE'
        GROUP BY r.homestayId
    """)
    List<HomestayRoomSummary> getRoomSummaries(@Param("homestayIds") List<Long> homestayIds);

    // BẢN CẬP NHẬT THEO KIẾN TRÚC CALENDAR - KHÓA PHÒNG REALTIME
    @Query(value = """
        SELECT r.id AS id, 
               MIN(rc.available_quantity) AS availableQty
        FROM homestay_rooms r
        JOIN room_calendar rc ON r.id = rc.room_id
        WHERE r.homestay_id = :homestayId
          AND r.status = 'ACTIVE'
          AND r.max_guests >= :guests
          AND rc.night_date >= :checkIn AND rc.night_date < :checkOut
        GROUP BY r.id
        -- CHỈNH SỬA ĐOẠN NÀY: Dùng CAST AS DATE thay cho dấu :: để Hibernate không bị lú
        HAVING MIN(rc.available_quantity) >= 1 
           AND COUNT(rc.id) = (CAST(:checkOut AS date) - CAST(:checkIn AS date))
        """, nativeQuery = true)
    List<RoomAvailabilityProjection> findAvailableRoomsProjections(
            @Param("homestayId") Long homestayId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("guests") int guests
    );

    List<HomestayRoom> findAllByHomestayIdAndStatus(Long homestayId, RoomStatus status);
}
