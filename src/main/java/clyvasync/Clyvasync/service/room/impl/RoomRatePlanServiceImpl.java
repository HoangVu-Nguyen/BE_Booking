package clyvasync.Clyvasync.service.room.impl;

import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.room.RoomRatePlan;
import clyvasync.Clyvasync.repository.room.RoomRatePlanRepository;
import clyvasync.Clyvasync.service.room.RoomRatePlanService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class RoomRatePlanServiceImpl implements RoomRatePlanService {
    private final RoomRatePlanRepository roomRatePlanRepository;

    @Override
    public List<RoomRatePlan> getAllRoomRatePlans(List<Long> roomIds) {
        return roomRatePlanRepository.findAllByRoomIdIn(roomIds);
    }

    @Override
    public RoomRatePlan getById(Long id) {
        return roomRatePlanRepository.findById(id).orElseThrow(()-> new AppException(ResultCode.ROOMS_NOT_FOUND));
    }
}
