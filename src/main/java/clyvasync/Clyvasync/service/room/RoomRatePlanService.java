package clyvasync.Clyvasync.service.room;

import clyvasync.Clyvasync.modules.room.RoomRatePlan;

import java.util.List;

public interface RoomRatePlanService {
    List<RoomRatePlan> getAllRoomRatePlans(List<Long> roomIds);

}
