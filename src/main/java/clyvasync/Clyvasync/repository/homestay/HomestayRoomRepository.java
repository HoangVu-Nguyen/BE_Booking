package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.dto.projection.RoomAvailabilityProjection;
import clyvasync.Clyvasync.dto.projection.RoomImageProjection;
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
    -- Đảm bảo không có ngày nào trong khoảng đó có available_quantity <= 0
    HAVING MIN(rc.available_quantity) >= 1
    """, nativeQuery = true)
    List<RoomAvailabilityProjection> findAvailableRoomsProjections(
            @Param("homestayId") Long homestayId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("guests") int guests
    );

    List<HomestayRoom> findAllByHomestayIdAndStatus(Long homestayId, RoomStatus status);
    List<HomestayRoom> findByIdIn(List<Long> ids);
    List<HomestayRoom> findAllByHomestayIdIn(List<Long> homestayIds);
    @Query("SELECT hi.imageUrl FROM HomestayImage hi WHERE hi.id IN :roomIds")
    List<String> findImageUrlsByIdIn(@Param("homestayIds") List<Long> roomIds);
    @Query("SELECT ri.id AS roomId, ri.imageUrl AS imageUrl FROM HomestayRoom ri WHERE ri.id IN :roomIds")
    List<RoomImageProjection> findRoomImagesByIdIn(@Param("roomIds") List<Long> roomIds);
    List<HomestayRoom> findAllByHomestayIdAndStatus(Long homestayId, String status);
}
