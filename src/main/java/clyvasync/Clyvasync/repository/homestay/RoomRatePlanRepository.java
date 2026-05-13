package clyvasync.Clyvasync.repository.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.RoomRatePlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRatePlanRepository extends JpaRepository<RoomRatePlan, Long> {
    List<RoomRatePlan> findAllByRoomIdIn(List<Long> roomIds);
}