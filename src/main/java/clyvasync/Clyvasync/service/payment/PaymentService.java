package clyvasync.Clyvasync.service.payment;

import clyvasync.Clyvasync.dto.event.BookingPaidEvent;
import clyvasync.Clyvasync.dto.request.PaymentConfirmRequest;
import clyvasync.Clyvasync.dto.response.PaymentConfirmResponse;
import clyvasync.Clyvasync.dto.response.UserPaymentMethodResponse;
import clyvasync.Clyvasync.enums.booking.BookingStatus;
import clyvasync.Clyvasync.enums.payment.PaymentMethod;
import clyvasync.Clyvasync.enums.type.PaymentStatus;
import clyvasync.Clyvasync.enums.type.PayoutStatus;
import clyvasync.Clyvasync.enums.type.TourBookingStatus;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.factory.PaymentFactory;
import clyvasync.Clyvasync.modules.booking.entity.Booking;
import clyvasync.Clyvasync.modules.booking.entity.BookingDetail;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayRoom;
import clyvasync.Clyvasync.modules.tour.entity.TourBooking;
import clyvasync.Clyvasync.repository.booking.BookingRepository;
import clyvasync.Clyvasync.service.booking.BookingDetailService;
import clyvasync.Clyvasync.service.booking.BookingService;
import clyvasync.Clyvasync.service.homestay.HomestayRoomService;
import clyvasync.Clyvasync.service.homestay.HomestayService;
import clyvasync.Clyvasync.service.notification.NotificationService;
import clyvasync.Clyvasync.service.tour.TourBookingService;
import clyvasync.Clyvasync.service.wallet.HostWalletService;
import clyvasync.Clyvasync.strategy.PaymentStrategy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public interface PaymentService {



    /**
     * Logic sinh link URL thanh toán
     */
     String createUrl(String bookingCode, PaymentMethod method, HttpServletRequest request);

    /**
     * Logic nghiệp vụ xử lý dữ liệu trả về cho Angular render UI thành công
     * FIX: Sử dụng đúng Service để lấy Room và Tours, không dùng hàm get quan hệ bị khuyết
     */
     Map<String, Object> processPaymentReturn(String gateway, Map<String, String> params);

    /**
     * Logic nghiệp vụ IPN Webhook chạy ngầm - Chốt đơn lưu trữ xuống DB thực tế
     * FIX: Đồng bộ cập nhật luôn trạng thái của tour_bookings đi kèm cho chuẩn luồng dữ liệu
     */
     ResponseEntity<?> processPaymentIPN(String gateway, Map<String, String> params) ;
    String createSetupIntent(Long userId);
   void savePaymentMethod(Long userId, String paymentMethodId);
    List<UserPaymentMethodResponse> getPaymentMethodsByUserId(Long userId);
    PaymentConfirmResponse processCheckoutPayment(Long userId, PaymentConfirmRequest request, HttpServletRequest httpServletRequest);
    Map<String, Object> getPaymentSuccessDetails(String bookingCode);
    /**
     * Gỡ bỏ và xóa thẻ tín dụng đã liên kết
     * @param userId ID của người dùng yêu cầu xóa (để check bảo mật chéo)
     * @param cardId ID vật lý (Primary Key) của thẻ trong bảng user_payment_methods
     */
    void deletePaymentMethod(Long userId, Long cardId);
}