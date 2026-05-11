package clyvasync.Clyvasync.service.booking.impl;

import clyvasync.Clyvasync.repository.booking.BookingRepository;
import clyvasync.Clyvasync.service.booking.BookingService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    @Override
    public boolean existsActiveBooking(Long userId, Long homestayId) {
        return bookingRepository.existsActiveBooking(userId,homestayId);
    }
}
