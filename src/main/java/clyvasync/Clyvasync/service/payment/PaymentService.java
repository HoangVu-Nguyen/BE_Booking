package clyvasync.Clyvasync.service.payment;

import clyvasync.Clyvasync.enums.payment.PaymentMethod;
import clyvasync.Clyvasync.enums.type.PaymentStatus;
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
import clyvasync.Clyvasync.service.tour.TourBookingService;
import clyvasync.Clyvasync.strategy.PaymentStrategy;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // Biện pháp Idempotency: Chống spam lặp giao dịch từ đối tác nếu đơn đã PAID trước đó
        if ("PAID".equalsIgnoreCase(booking.getPaymentStatus())) {
            return strategy.buildIPNSuccessResponse("Đơn hàng này đã được xác nhận thành công từ trước");
        }

        // Chốt đơn thực sự xuống Database hệ thống
        if (paymentStatus == PaymentStatus.PAID) {
            // 1. Cập nhật trạng thái tổng của Booking thành PAID
            booking.setPaymentStatus("PAID");
            booking.setStatus("CONFIRMED"); // Đổi trạng thái từ DRAFT sang CONFIRMED luôn cho khớp luồng
            bookingRepository.save(booking);

            // 2. Cập nhật luôn trạng thái đóng dấu ĐÃ THÀNH TOÁN cho các tour đi kèm của đơn hàng này
            List<TourBooking> tourBookings = tourBookingService.findAllByHomestayBookingId(booking.getId());
            if (tourBookings != null && !tourBookings.isEmpty()) {
                for (TourBooking tb : tourBookings) {
                    tb.setPaymentStatus(PaymentStatus.PAID);
                    tb.setStatus(clyvasync.Clyvasync.enums.type.TourBookingStatus.CONFIRMED); // Ép chuẩn Enum của bác
                    tourBookingService.save(tb); // Lưu lại trạng thái tour
                }
            }

            log.info("[IPN SUCCESS] Đơn hàng {} và các dịch vụ tour đi kèm đã được chốt trạng thái PAID thành công!", bookingCode);
            return strategy.buildIPNSuccessResponse("Xác nhận đơn thành công - Đã cập nhật trạng thái PAID");
        } else {
            booking.setPaymentStatus("FAILED");
            bookingRepository.save(booking);
            return strategy.buildIPNSuccessResponse("Xác nhận đơn thành công - Giao dịch gốc thất bại");
        }
    }
}