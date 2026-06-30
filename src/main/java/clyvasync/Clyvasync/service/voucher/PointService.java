package clyvasync.Clyvasync.service.voucher;

public interface PointService {
    void addPointsFromBooking(Long userId, Integer points, Long bookingId, String description);
    void deductPointsForVoucher(Long userId, Integer points, String description);
    void deductPointsForBookingCancellation(Long userId, Integer points, Long bookingId, String description);
}
