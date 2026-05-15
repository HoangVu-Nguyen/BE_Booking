package clyvasync.Clyvasync.service.tour.impl;

import clyvasync.Clyvasync.dto.request.BookTourRequest;
import clyvasync.Clyvasync.dto.response.TourBookingResponse;
import clyvasync.Clyvasync.enums.type.PaymentStatus;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.tour.entity.TourBooking;
import clyvasync.Clyvasync.repository.tour.TourBookingRepository;
import clyvasync.Clyvasync.service.tour.TourBookingService;
import lombok.AllArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@AllArgsConstructor
public class TourBookingServiceImpl implements TourBookingService {
    private final TourBookingRepository tourBookingRepository;
    @Override
    public TourBookingResponse bookStandaloneTour(Long userId, BookTourRequest request) {
        return null;
    }

    @Override
    public TourBookingResponse bookTourWithHomestay(Long userId, Long homestayBookingId, BookTourRequest request) {
        return null;
    }

    @Override
    public void updatePaymentStatus(String bookingCode, PaymentStatus paymentStatus) {

    }

    @Override
    public void cancelBooking(Long bookingId, Long userId, String cancelReason) {

    }

    @Override
    public TourBookingResponse getBookingById(Long bookingId) {
        return null;
    }

    @Override
    public Page<TourBookingResponse> getUserBookingHistory(Long userId, Pageable pageable) {
        return null;
    }

    @Override
    public Page<TourBookingResponse> getBookingsForHomestayOwner(Long ownerId, Pageable pageable) {
        return null;
    }

    @Override
    public TourBooking save(TourBooking tourBooking) {
        return tourBookingRepository.save(tourBooking);
    }

    @Override
    public TourBooking findByHomestayBookingId(Long homestayBookingId) {
        return tourBookingRepository.findByHomestayBookingId(homestayBookingId).orElse(null);
    }

    @Override
    public List<TourBooking> findAllByHomestayBookingId(Long homestayBookingId) {
        return tourBookingRepository.findAllByHomestayBookingId(homestayBookingId);
    }
}
