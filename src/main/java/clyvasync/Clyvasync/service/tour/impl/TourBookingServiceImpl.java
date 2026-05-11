package clyvasync.Clyvasync.service.tour.impl;

import clyvasync.Clyvasync.dto.request.BookTourRequest;
import clyvasync.Clyvasync.dto.response.TourBookingResponse;
import clyvasync.Clyvasync.enums.type.PaymentStatus;
import clyvasync.Clyvasync.service.tour.TourBookingService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class TourBookingServiceImpl implements TourBookingService {
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
}
