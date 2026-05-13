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
    @Query(value = """
SELECT temp.id AS id, temp.availableQty AS availableQty FROM (
    SELECT r.id, 
           (r.quantity - COALESCE(booked.total_booked, 0)) as availableQty
    FROM homestay_rooms r
    LEFT JOIN (
        SELECT bd.room_id, SUM(bd.quantity) as total_booked
        FROM booking_details bd
        JOIN bookings b ON b.id = bd.booking_id
        WHERE b.status IN ('CONFIRMED', 'PENDING')
          AND bd.check_in_date < :checkOut 
          AND bd.check_out_date > :checkIn
        GROUP BY bd.room_id
    ) booked ON r.id = booked.room_id
    WHERE r.homestay_id = :homestayId 
      AND r.status = 'ACTIVE' 
      AND r.max_guests >= :guests
) temp
WHERE temp.availableQty > 0
""", nativeQuery = true)
    List<RoomAvailabilityProjection> findAvailableRoomsProjections(
            @Param("homestayId") Long homestayId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("guests") int guests
    );

    List<HomestayRoom> findAllByHomestayIdAndStatus(Long homestayId, RoomStatus status);
}
