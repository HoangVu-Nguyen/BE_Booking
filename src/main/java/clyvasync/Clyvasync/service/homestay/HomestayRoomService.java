package clyvasync.Clyvasync.service.homestay;

import clyvasync.Clyvasync.dto.response.RoomResponse;
import clyvasync.Clyvasync.dto.summary.HomestayRoomSummary;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface HomestayRoomService {
    List<RoomResponse> getAllRoomsByHomestay(Long homestayId);

    List<RoomResponse> findAvailableRooms(Long homestayId, LocalDate checkIn, LocalDate checkOut, int guests);
    List<HomestayRoomSummary> getRoomSummaries( List<Long> homestayIds);
}
