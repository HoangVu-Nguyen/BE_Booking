package clyvasync.Clyvasync.repository.room;

import clyvasync.Clyvasync.modules.room.RoomCalendar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomCalendarRepository extends JpaRepository<RoomCalendar, Long> {
    RoomCalendar findByRoomId(Long roomId);
    @Modifying
    @Query("""
        UPDATE RoomCalendar rc 
        SET rc.availableQuantity = rc.availableQuantity - :qty
        WHERE rc.roomId = :roomId 
          AND rc.nightDate >= :checkIn AND rc.nightDate < :checkOut
          AND rc.availableQuantity >= :qty
        """)
    int lockRoomRange(@Param("roomId") Long roomId,
                      @Param("checkIn") LocalDate checkIn,
                      @Param("checkOut") LocalDate checkOut,
                      @Param("qty") int qty);

    @Modifying
    @Query("""
        UPDATE RoomCalendar rc 
        SET rc.availableQuantity = rc.availableQuantity + :qty
        WHERE rc.roomId = :roomId 
          AND rc.nightDate >= :checkIn AND rc.nightDate < :checkOut
        """)
    int unlockRoomRange(@Param("roomId") Long roomId,
                        @Param("checkIn") LocalDate checkIn,
                        @Param("checkOut") LocalDate checkOut,
                        @Param("qty") int qty);
    @Query("""
        SELECT rc.nightDate 
        FROM RoomCalendar rc 
        WHERE rc.roomId = :roomId 
          AND rc.nightDate BETWEEN :start AND :end 
          AND rc.availableQuantity <= 0
        """)
    List<LocalDate> findUnavailableDates(@Param("roomId") Long roomId,
                                         @Param("start") LocalDate start,
                                         @Param("end") LocalDate end);
}
