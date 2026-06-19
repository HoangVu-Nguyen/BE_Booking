package clyvasync.Clyvasync.service.room.impl;

import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayRoom;
import clyvasync.Clyvasync.modules.room.RoomRatePlan;
import clyvasync.Clyvasync.repository.homestay.HomestayRoomRepository;
import clyvasync.Clyvasync.repository.room.RoomRatePlanRepository;
import clyvasync.Clyvasync.service.homestay.HomestayService;
import clyvasync.Clyvasync.service.room.RoomRatePlanService;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.util.List;

@Service

public class RoomRatePlanServiceImpl implements RoomRatePlanService {
    private final RoomRatePlanRepository roomRatePlanRepository;
    private final HomestayService homestayService;
    private final HomestayRoomRepository homestayRoomRepository;

    public RoomRatePlanServiceImpl(RoomRatePlanRepository roomRatePlanRepository, @Lazy HomestayService homestayService, HomestayRoomRepository homestayRoomRepository) {
        this.roomRatePlanRepository = roomRatePlanRepository;
        this.homestayService = homestayService;
        this.homestayRoomRepository = homestayRoomRepository;
    }


    @Override
    public List<RoomRatePlan> getAllRoomRatePlans(List<Long> roomIds) {
        return roomRatePlanRepository.findAllByRoomIdIn(roomIds);
    }

    @Override
    public RoomRatePlan getById(Long id) {
        return roomRatePlanRepository.findById(id).orElseThrow(()-> new AppException(ResultCode.ROOMS_NOT_FOUND));
    }
    @Override
    public void validateRoomAndRatePlan(
            Long ownerId,
            Long homestayId,
            Long roomId,
            Long ratePlanId
    ) {
        Homestay homestay = homestayService.findById(homestayId);
        if (!homestay.getOwnerId().equals(ownerId)) {
            throw new AppException(ResultCode.ACCESS_DENIED);
        }
        HomestayRoom room = homestayRoomRepository
                .findByIdAndHomestayId(roomId, homestayId)
                .orElseThrow(() -> new AppException(ResultCode.ROOM_NOT_FOUND));



        RoomRatePlan ratePlan = roomRatePlanRepository
                .findByIdAndRoomId(ratePlanId, roomId)
                .orElseThrow(() -> new AppException(ResultCode.ROOMS_NOT_FOUND));
    }
}
