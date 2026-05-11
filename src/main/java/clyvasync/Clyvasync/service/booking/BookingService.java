package clyvasync.Clyvasync.service.booking;

public interface BookingService {
    boolean existsActiveBooking(Long userId,Long homestayId);
}
