package clyvasync.Clyvasync.service.payment;

import clyvasync.Clyvasync.dto.event.BookingPaidEvent;
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

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentFactory paymentFactory;
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    // Inject thêm các Service bác đã viết để cào dữ liệu chuẩn xác
    private final BookingDetailService bookingDetailService;
    private final HomestayRoomService homestayRoomService;
    private final TourBookingService tourBookingService;
    private final HostWalletService hostWalletService;
    private final HomestayService homestayService;
    private final ApplicationEventPublisher eventPublisher; // Kênh phát thanh sự kiện

    /**
     * Logic sinh link URL thanh toán
     */
    public String createUrl(String bookingCode, PaymentMethod method, HttpServletRequest request) {
        Booking booking = bookingService.getBookingByCode(bookingCode);
        PaymentStrategy strategy = paymentFactory.getStrategy(method);
        return strategy.createPaymentUrl(booking, request);
    }

    /**
     * Logic nghiệp vụ xử lý dữ liệu trả về cho Angular render UI thành công
     * FIX: Sử dụng đúng Service để lấy Room và Tours, không dùng hàm get quan hệ bị khuyết
     */
    public Map<String, Object> processPaymentReturn(String gateway, Map<String, String> params) {
        PaymentMethod method = PaymentMethod.valueOf(gateway.toUpperCase());
        PaymentStrategy strategy = paymentFactory.getStrategy(method);

        // 1. Xác thực chữ ký số từ đối tác tránh đổi tham số URL ở FE
        PaymentStatus status = strategy.processCallback(params);
        String bookingCode = strategy.extractBookingCode(params);

        Booking booking = bookingService.getBookingByCode(bookingCode);

        if (status == PaymentStatus.PAID) {
            Map<String, Object> data = new HashMap<>();
            data.put("bookingCode", booking.getBookingCode());
            data.put("totalPrice", booking.getTotalPrice());
            data.put("guestName", booking.getGuestName());

            // 2. Lấy thông tin phòng chuẩn chỉ bằng bookingDetailService bác cung cấp
            BookingDetail detail = bookingDetailService.findBookingDetailByBookingId(booking.getId());
            if (detail != null) {
                HomestayRoom room = homestayRoomService.getRoomById(detail.getRoomId());
                data.put("roomName", room != null ? room.getName() : "N/A");
                data.put("guests", detail.getGuestCount() + " Guests");
            }

            // 3. Lấy danh sách tour đính kèm từ đúng tourBookingService của hệ thống
            List<TourBooking> tourBookings = tourBookingService.findAllByHomestayBookingId(booking.getId());
            data.put("tours", tourBookings);

            return data;
        }

        throw new AppException(ResultCode.PAYMENT_FAILED_OR_CANCELLED);
    }

    /**
     * Logic nghiệp vụ IPN Webhook chạy ngầm - Chốt đơn lưu trữ xuống DB thực tế
     * FIX: Đồng bộ cập nhật luôn trạng thái của tour_bookings đi kèm cho chuẩn luồng dữ liệu
     */
    @Transactional
    public ResponseEntity<?> processPaymentIPN(String gateway, Map<String, String> params) {
        PaymentMethod method = PaymentMethod.valueOf(gateway.toUpperCase());
        PaymentStrategy strategy = paymentFactory.getStrategy(method);

        PaymentStatus paymentStatus = strategy.processCallback(params);
        String bookingCode = strategy.extractBookingCode(params);
        Booking booking = bookingService.getBookingByCode(bookingCode);

        // Idempotency check
        if (PaymentStatus.PAID.equals(booking.getPaymentStatus())) {
            return strategy.buildIPNSuccessResponse("Đơn hàng này đã được xác nhận thành công từ trước");
        }

        if (paymentStatus == PaymentStatus.PAID) {
            // 1. Chỉ cập nhật đúng trạng thái của Booking
            booking.setPaymentStatus(PaymentStatus.PAID);
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            // 2. Phát loa thông báo: "Đơn này đã trả tiền xong!"
            log.info("[IPN SUCCESS] Đơn hàng {} chốt PAID. Kích hoạt luồng hậu thanh toán...", bookingCode);
            eventPublisher.publishEvent(new BookingPaidEvent(booking));


            return strategy.buildIPNSuccessResponse("Xác nhận đơn thành công");
        } else {
            booking.setPaymentStatus(PaymentStatus.UNPAID);
            bookingRepository.save(booking);
            return strategy.buildIPNSuccessResponse("Giao dịch gốc thất bại");
        }
    }
}