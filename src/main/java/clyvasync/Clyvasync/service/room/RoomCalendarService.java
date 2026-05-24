package clyvasync.Clyvasync.service.room;

import clyvasync.Clyvasync.dto.request.BatchUpdateCalendarRequest;
import clyvasync.Clyvasync.dto.response.CalendarInventoryResponse;
import clyvasync.Clyvasync.dto.response.CalendarRoomResponse;
import clyvasync.Clyvasync.dto.response.HomestayCalendarResponse;
import clyvasync.Clyvasync.modules.room.RoomCalendar;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface RoomCalendarService {

    int lockRoomRange( Long roomId,
                     LocalDate checkIn,
                      LocalDate checkOut,
                     int qty);


    int unlockRoomRange( Long roomId,
                        LocalDate checkIn,
                       LocalDate checkOut,
                         int qty);
     List<LocalDate> getUnavailableDates(Long roomId, int month, int year);
    List<RoomCalendar> findCalendarsByRoomIdsAndDateRange(List<Long> roomIds, LocalDate startDate,LocalDate endDate);
    List<RoomCalendar> findCustomCalendarByRoomIdsAndDateRange(
            List<Long> roomIds,
        LocalDate startDate,
             LocalDate endDate
    );
    HomestayCalendarResponse getHomestayCalendar(Long ownerId,Long homestayId, LocalDate startDate, LocalDate endDate);
    void batchUpdateCalendar(BatchUpdateCalendarRequest request);
    List<CalendarInventoryResponse> getCalendarDetails(Long roomId, LocalDate start, LocalDate end);
}
