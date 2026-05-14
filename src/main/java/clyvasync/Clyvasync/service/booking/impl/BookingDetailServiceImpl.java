package clyvasync.Clyvasync.service.booking.impl;

import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.booking.entity.BookingDetail;
import clyvasync.Clyvasync.repository.booking.BookingDetailRepository;
import clyvasync.Clyvasync.service.booking.BookingDetailService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@AllArgsConstructor
public class BookingDetailServiceImpl implements BookingDetailService {
    private final BookingDetailRepository bookingDetailRepository;

    @Override
    public List<BookingDetail> findOverlappingBookings(Long roomId, LocalDate startOfMonth, LocalDate endOfMonth) {
        return bookingDetailRepository.findOverlappingBookings(roomId, startOfMonth, endOfMonth);
    }

    @Override
    public BookingDetail save(BookingDetail bookingDetail) {
        return bookingDetailRepository.save(bookingDetail);
    }

    @Override
    public BookingDetail findBookingDetailByBookingId(Long bookingId) {
        return bookingDetailRepository.findById(bookingId).orElseThrow(() -> new AppException(ResultCode.BOOKING_NOT_FOUND));
    }

}
