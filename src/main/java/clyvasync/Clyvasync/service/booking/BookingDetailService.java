package clyvasync.Clyvasync.service.booking;

import clyvasync.Clyvasync.modules.booking.entity.BookingDetail;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingDetailService {
    List<BookingDetail> findOverlappingBookings(
            Long roomId,
            LocalDate startOfMonth,
            LocalDate endOfMonth
    );
    BookingDetail save(BookingDetail bookingDetail);
    BookingDetail findBookingDetailByBookingId(Long bookingId);
}
