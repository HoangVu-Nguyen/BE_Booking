package clyvasync.Clyvasync.service.room;

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
}
