package clyvasync.Clyvasync.service.homestay;

import clyvasync.Clyvasync.dto.response.RoomResponse;

import java.time.LocalDate;
import java.util.List;

public interface HomestayRoomService {
    List<RoomResponse> getAllRoomsByHomestay(Long homestayId);

    List<RoomResponse> findAvailableRooms(Long homestayId, LocalDate checkIn, LocalDate checkOut, int guests);
}
