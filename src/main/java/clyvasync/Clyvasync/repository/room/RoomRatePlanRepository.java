package clyvasync.Clyvasync.repository.room;

import clyvasync.Clyvasync.modules.room.RoomRatePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRatePlanRepository extends JpaRepository<RoomRatePlan, Long> {
    List<RoomRatePlan> findAllByRoomIdIn(List<Long> roomIds);
}