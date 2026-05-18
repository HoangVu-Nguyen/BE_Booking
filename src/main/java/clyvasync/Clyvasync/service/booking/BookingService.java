package clyvasync.Clyvasync.service.booking;

import clyvasync.Clyvasync.dto.request.BookingInitRequest;
import clyvasync.Clyvasync.dto.response.BookingDetailsResponse;
import clyvasync.Clyvasync.dto.response.BookingInitResponse;
import clyvasync.Clyvasync.enums.booking.BookingStatus;
import clyvasync.Clyvasync.modules.booking.entity.Booking;

import java.time.LocalDate;
import java.util.List;

public interface BookingService {
    boolean existsActiveBooking(Long userId,Long homestayId);
    List<LocalDate> getUnavailableDates(Long homestayId, int month, int year) ;
    BookingInitResponse initBooking(BookingInitRequest request, Long userId);
    BookingDetailsResponse getBookingDetailsByCode(String bookingCode);
    Booking getBookingByCode(String bookingCode);
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    Booking findByBookingCodeAndUserId(String bookingCode, Long currentUserId);
    List<Booking> findAllByUserIdAndStatusOrderByUpdatedAtDesc(Long userId, BookingStatus status);
}
