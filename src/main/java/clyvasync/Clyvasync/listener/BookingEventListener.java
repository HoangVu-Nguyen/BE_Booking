package clyvasync.Clyvasync.listener;

import clyvasync.Clyvasync.constant.SocketDestinations;

import clyvasync.Clyvasync.dto.detail.CancellationMailMessage;
import clyvasync.Clyvasync.dto.event.BookingCancelledEvent;
import clyvasync.Clyvasync.dto.event.BookingEvent;
import clyvasync.Clyvasync.enums.type.NotificationType;
import clyvasync.Clyvasync.modules.booking.entity.Booking;
import clyvasync.Clyvasync.modules.booking.entity.BookingDetail;
import clyvasync.Clyvasync.service.booking.BookingDetailService;
import clyvasync.Clyvasync.service.homestay.HomestayService;
import clyvasync.Clyvasync.service.mail.MailService;
import clyvasync.Clyvasync.service.notification.NotificationService;
import clyvasync.Clyvasync.service.realtime.SocketEmitterService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingEventListener {

    private final SocketEmitterService socketEmitterService;
    private final NotificationService notificationService;
    private final HomestayService homestayService;
    private final MailService mailService;
    private final BookingDetailService bookingDetailService;


    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBookingRealtimeNotification(BookingEvent event) {
        log.info("[BOOKING-LISTENER] DB commit đặt phòng thành công. Tiến hành đẩy realtime.");

        // 1. Bắn tin nhắn riêng tư (Private) cho Host nhận đơn
        socketEmitterService.sendBookingNotification(event.getHostId(), event.getBookingPayload());

        // 2. Phát tín hiệu công khai (Broadcast) để đồng bộ trạng thái phòng/lịch phòng cho toàn hệ thống
        // Ví dụ payload chứa { roomId: 10, unavailableDates: ["2026-05-25", "2026-05-26"] }
        socketEmitterService.broadcastRoomStatus(SocketDestinations.ROOM_STATUS_TOPIC, event.getBookingPayload());


    }
    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBookingCancelled(BookingCancelledEvent event) {
        Booking booking = event.booking();
        log.info("[EVENT-ASYNC] Tiếp nhận xử lý hậu kỳ cho đơn hủy: {}", booking.getBookingCode());

        try {
            // 1. Tìm thông tin chủ nhà (Host) và Homestay
            var homestay = homestayService.findById(booking.getHomestayId());
            Long hostId = homestay.getOwnerId();
            BookingDetail bookingDetail = bookingDetailService.findBookingDetailByBookingId(booking.getId());

            // 2. Đẩy thông báo Real-time cho Host
            notificationService.sendNotification(
                    hostId,
                    NotificationType.BOOKING_CANCELLED,
                    "Đơn đặt phòng đã bị hủy",
                    String.format("Khách hàng %s đã chủ động hủy chuyến đi %s.", booking.getGuestName(), booking.getBookingCode()),
                    Map.of("bookingCode", booking.getBookingCode())
            );
            notificationService.sendNotification(
                    booking.getUserId(),
                    NotificationType.BOOKING_CANCELLED,
                    "Hủy đơn thành công",
                    String.format("Đơn hàng %s của bạn đã được hủy thành công. Vui lòng kiểm tra email để biết chi tiết hoàn tiền.", booking.getBookingCode()),
                    Map.of("bookingCode", booking.getBookingCode())
            );

            // Chuẩn bị formatter tiền tệ Việt Nam
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            var preview = event.cancelPreviewResponse();

            // Xử lý logic hiển thị tiền (Tránh null nếu đơn chưa thanh toán)
            String strTotalPaid = (preview != null && preview.getTotalPaid() != null) ? currencyFormat.format(preview.getTotalPaid()) : "0 ₫";
            String strRefundAmount = (preview != null && preview.getRefundAmount() != null) ? currencyFormat.format(preview.getRefundAmount()) : "0 ₫";
            String strPenaltyFee = (preview != null && preview.getPenaltyFee() != null) ? currencyFormat.format(preview.getPenaltyFee()) : "0 ₫";
            String policyMsg = (preview != null && preview.getRefundPolicyMessage() != null) ? preview.getRefundPolicyMessage() : "Đơn hàng chưa thanh toán hoặc không áp dụng hoàn tiền.";

            // 2.5. Đóng gói DTO cho MailService
            CancellationMailMessage mailMsg = CancellationMailMessage.builder()
                    .guestName(booking.getGuestName())
                    .guestEmail(booking.getGuestEmail())
                    .bookingCode(booking.getBookingCode())
                    .homestayName(homestay.getName())
                    .checkInDate(bookingDetail.getCheckInDate().toString()) // Có thể format lại date nếu cần
                    .cancelReason("Khách hàng chủ động hủy qua ứng dụng")

                    // Nạp data tài chính vào Mail Message
                    .totalPaid(strTotalPaid)
                    .refundAmount(strRefundAmount)
                    .penaltyFee(strPenaltyFee)
                    .refundPolicyMessage(policyMsg)
                    .build();

            // 3. Đẩy tác vụ gửi Mail
            mailService.sendCancellationEmail(mailMsg);

        } catch (Exception e) {
            log.error("[EVENT-ERROR] Lỗi xử lý tác vụ nền sau khi hủy đơn {}: ", booking.getBookingCode(), e);
        }
    }
}