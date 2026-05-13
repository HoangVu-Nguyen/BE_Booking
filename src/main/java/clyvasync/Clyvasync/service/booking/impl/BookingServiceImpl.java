package clyvasync.Clyvasync.service.booking.impl;

import clyvasync.Clyvasync.modules.booking.entity.Booking;
import clyvasync.Clyvasync.repository.booking.BookingRepository;
import clyvasync.Clyvasync.service.booking.BookingService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    @Override
    public boolean existsActiveBooking(Long userId, Long homestayId) {
        return true  ;
    }

    @Override
    public List<LocalDate> getUnavailableDates(Long homestayId, int month, int year) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDate endDate = startDate.plusMonths(2).withDayOfMonth(1);
        List<Booking> bookings = bookingRepository.findOverlappingBookings(
                homestayId, startDate, endDate, List.of("CONFIRMED", "PENDING")
        );
        Set<LocalDate> blockedDates = new HashSet<>();
        for (Booking booking : bookings) {
            LocalDate currentDate = booking.getCheckInDate();
            // Chú ý: Dùng isBefore thay vì isBeforeOrEquals để chừa lại ngày Check-out
            while (currentDate.isBefore(booking.getCheckOutDate())) {
                blockedDates.add(currentDate);
                currentDate = currentDate.plusDays(1);
            }
        }
        return  blockedDates.stream().sorted().toList();
    }
}
