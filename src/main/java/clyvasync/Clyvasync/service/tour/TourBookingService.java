package clyvasync.Clyvasync.service.tour;

import clyvasync.Clyvasync.dto.request.BookTourRequest;
import clyvasync.Clyvasync.dto.response.TourBookingResponse;
import clyvasync.Clyvasync.enums.type.PaymentStatus;
import clyvasync.Clyvasync.modules.tour.entity.TourBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TourBookingService {
    // ==========================================
    // LUỒNG ĐẶT TOUR
    // ==========================================

    /**
     * Khách hàng đặt Tour lẻ (Không book phòng, chỉ book tour)
     */
    TourBookingResponse bookStandaloneTour(Long userId, BookTourRequest request);

    /**
     * Khách hàng đặt Tour KÈM VỚI lúc book phòng Homestay
     * (Thường được gọi ngầm từ HomestayCheckoutService)
     */
    TourBookingResponse bookTourWithHomestay(Long userId, Long homestayBookingId, BookTourRequest request);

    // ==========================================
    // QUẢN LÝ TRẠNG THÁI GIAO DỊCH
    // ==========================================

    /** Cập nhật trạng thái thanh toán (Thường được gọi bởi Webhook từ VNPAY/MoMo) */
    void updatePaymentStatus(String bookingCode, PaymentStatus paymentStatus);

    /** Khách hàng yêu cầu Hủy Tour */
    void cancelBooking(Long bookingId, Long userId, String cancelReason);

    // ==========================================
    // TRA CỨU / THỐNG KÊ LỊCH SỬ
    // ==========================================

    /** Lấy chi tiết 1 mã đặt Tour */
    TourBookingResponse getBookingById(Long bookingId);

    /** Khách hàng xem lịch sử các Tour mình đã đặt */
    Page<TourBookingResponse> getUserBookingHistory(Long userId, Pageable pageable);

    /** Chủ Homestay xem danh sách khách đã đặt Tour của mình (Để chuẩn bị đón khách) */
    Page<TourBookingResponse> getBookingsForHomestayOwner(Long ownerId, Pageable pageable);
    TourBooking save(TourBooking tourBooking);
    TourBooking findByHomestayBookingId(Long homestayBookingId);
}
