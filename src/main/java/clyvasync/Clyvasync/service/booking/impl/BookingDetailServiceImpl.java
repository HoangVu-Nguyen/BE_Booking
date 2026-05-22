package clyvasync.Clyvasync.service.booking.impl;

import clyvasync.Clyvasync.dto.projection.BookingCalendarProjection;
import clyvasync.Clyvasync.dto.projection.BookingTimelineProjection;
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
        return bookingDetailRepository.findBookingDetailByBookingId(bookingId).orElseThrow(() -> new AppException(ResultCode.BOOKING_NOT_FOUND));
    }

    @Override
    public List<BookingDetail> findByBookingIdIn(List<Long> bookingIds) {
        return bookingDetailRepository.findByBookingIdIn(bookingIds);
    }

    @Override
    public List<BookingDetail> findAllByBookingId(Long bookingId) {
        return bookingDetailRepository.findByBookingId(bookingId);
    }

    @Override
    public List<BookingTimelineProjection> findOverlappingBookings(List<Long> roomIds, LocalDate startOfMonth, LocalDate endOfMonth) {
        return bookingDetailRepository.findOverlappingBookings(roomIds, startOfMonth, endOfMonth);
    }

    @Override
    public List<BookingDetail> findActiveBookingsByRoomIdsAndDateRange(List<Long> roomIds, LocalDate startDate, LocalDate endDate) {
        return bookingDetailRepository.findActiveBookingsByRoomIdsAndDateRange(roomIds, startDate, endDate);
    }

    @Override
    public List<BookingCalendarProjection> findActiveBookingsForCalendar(List<Long> roomIds, LocalDate startDate, LocalDate endDate) {
        return bookingDetailRepository.findActiveBookingsForCalendar(roomIds,startDate, endDate);
    }

}
