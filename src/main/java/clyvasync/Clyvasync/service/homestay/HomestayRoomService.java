package clyvasync.Clyvasync.service.homestay;

import clyvasync.Clyvasync.dto.request.BatchUploadRequest;
import clyvasync.Clyvasync.dto.request.MultiRoomBatchUploadRequest;
import clyvasync.Clyvasync.dto.request.RoomBatchUpdateRequest;
import clyvasync.Clyvasync.dto.response.PresignedUrlResponse;
import clyvasync.Clyvasync.dto.response.RoomDisplayResponse;
import clyvasync.Clyvasync.dto.response.RoomResponse;
import clyvasync.Clyvasync.dto.response.RoomUpdateResponse;
import clyvasync.Clyvasync.dto.summary.HomestayRoomSummary;
import clyvasync.Clyvasync.enums.room.RoomStatus;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayRoom;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface HomestayRoomService {
    List<RoomResponse> getAllRoomsByHomestay(Long homestayId);

    List<RoomResponse> findAvailableRooms(Long homestayId, LocalDate checkIn, LocalDate checkOut, int guests);
    List<HomestayRoomSummary> getRoomSummaries( List<Long> homestayIds);
    HomestayRoom getRoomById(Long roomId);
    List<HomestayRoom> findByIdIn(List<Long> roomIds);
    List<HomestayRoom> findAllById(Long homestayId);
    List<HomestayRoom> findAllByIdIn(List<Long> homestayIds);
    Map<Long, String> getRoomImageMap(List<Long> roomIds);
    List<HomestayRoom> findAllByHomestayIdAndStatus(Long homestayId, RoomStatus status);
    List<RoomDisplayResponse> getRoomsByHomestayId(Long homestayId);
    void updateRooms(Long userId,RoomBatchUpdateRequest request);
    List<PresignedUrlResponse> prepareHomestayRoomImageBatch(Long ownerId, MultiRoomBatchUploadRequest request);}
