package clyvasync.Clyvasync.repository.homestay;

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
    @Query("""
        SELECT r FROM HomestayRoom r 
        WHERE r.homestayId = :homestayId 
          AND r.status = 'ACTIVE' 
          AND r.maxGuests >= :guests
          AND r.id NOT IN (
              SELECT bd.roomId FROM BookingDetail bd 
              JOIN Booking b ON b.id = bd.bookingId
              WHERE b.status IN ('CONFIRMED', 'PENDING')
                AND bd.checkInDate < :checkOut 
                AND bd.checkOutDate > :checkIn
          )
    """)
    List<HomestayRoom> findAvailableRooms(
            @Param("homestayId") Long homestayId,
            @Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("guests") int guests
    );

    List<HomestayRoom> findAllByHomestayIdAndStatus(Long homestayId, RoomStatus status);
}
