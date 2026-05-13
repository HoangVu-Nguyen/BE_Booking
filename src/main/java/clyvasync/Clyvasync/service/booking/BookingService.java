package clyvasync.Clyvasync.service.booking;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    boolean existsActiveBooking(Long userId,Long homestayId);
    List<LocalDate> getUnavailableDates(Long homestayId, int month, int year) ;
}
