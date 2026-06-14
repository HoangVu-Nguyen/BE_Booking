package clyvasync.Clyvasync.repository.room;

import clyvasync.Clyvasync.modules.room.RoomBed;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface RoomBedRepository extends JpaRepository<RoomBed, Long> {
    @Modifying
    @Query("DELETE FROM RoomBed b WHERE b.roomId = :roomId")
    void deleteByRoomId(Long roomId);
    List<RoomBed> findByRoomIdIn(List<Long> roomIds);
}
