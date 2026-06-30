package clyvasync.Clyvasync.listener;


import clyvasync.Clyvasync.dto.event.BookingPaidEvent;
import clyvasync.Clyvasync.enums.type.NotificationType;
import clyvasync.Clyvasync.enums.type.PaymentStatus;
import clyvasync.Clyvasync.enums.type.PayoutStatus;
import clyvasync.Clyvasync.enums.type.TourBookingStatus;
import clyvasync.Clyvasync.modules.booking.entity.Booking;
import clyvasync.Clyvasync.modules.tour.entity.TourBooking;
import clyvasync.Clyvasync.repository.booking.BookingRepository;
import clyvasync.Clyvasync.service.homestay.HomestayService;
import clyvasync.Clyvasync.service.notification.NotificationService;
import clyvasync.Clyvasync.service.realtime.SocketEmitterService;
import clyvasync.Clyvasync.service.tour.TourBookingService;
import clyvasync.Clyvasync.service.wallet.HostWalletService;
import clyvasync.Clyvasync.service.voucher.PointService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class PostPaymentWorkflowListener {

    private final HostWalletService hostWalletService;
    private final HomestayService homestayService;
    private final TourBookingService tourBookingService;
    private final BookingRepository bookingRepository;
    private final SocketEmitterService socketEmitterService;
    private final NotificationService notificationService;
    private final PointService pointService;

    // Kéo cấu hình phí hoa hồng từ application.properties (không hardcode)
    @Value("${app.platform.fee-percentage:0.10}")
    private BigDecimal platformFeePercent;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleBookingPaidEscrowAndTours(BookingPaidEvent event) {
        Booking booking = event.booking();

        try {

            // 1. TÍNH TOÁN DÒNG TIỀN VÀ KÝ QUỸ
            BigDecimal platformFee = booking.getTotalPrice().multiply(platformFeePercent);
            BigDecimal hostPayout = booking.getTotalPrice().subtract(platformFee);

            booking.setPlatformFeeAmount(platformFee);
            booking.setHostPayoutAmount(hostPayout);
            booking.setPayoutStatus(PayoutStatus.ON_HOLD);
            bookingRepository.save(booking);

            var homestay = homestayService.findById(booking.getHomestayId());
            hostWalletService.lockFundsForBooking(booking.getId(), homestay.getOwnerId(), hostPayout);

            // 2. CẬP NHẬT TRẠNG THÁI TOUR ĐI KÈM
            List<TourBooking> tourBookings = tourBookingService.findAllByHomestayBookingId(booking.getId());
            if (tourBookings != null && !tourBookings.isEmpty()) {
                for (TourBooking tb : tourBookings) {
                    tb.setPaymentStatus(PaymentStatus.PAID);
                    tb.setStatus(TourBookingStatus.CONFIRMED);
                    tourBookingService.save(tb);
                }
            }
            log.info("Luồng Hậu thanh toán hoàn tất cho Booking: {}", booking.getBookingCode());
            
            // 2.5 CỘNG ĐIỂM THƯỞNG CHO USER KHI ĐÃ THANH TOÁN THÀNH CÔNG
            try {
                // Tỉ lệ mặc định: 100,000 VND = 1 điểm.
                Integer pointsEarned = booking.getTotalPrice().intValue() / 100000;
                if (pointsEarned > 0) {
                    pointService.addPointsFromBooking(
                        booking.getUserId(), 
                        pointsEarned, 
                        booking.getId(), 
                        "Tích điểm từ đơn đặt phòng #" + booking.getBookingCode()
                    );
                    log.info("Đã cộng {} điểm cho user {} từ booking {}", pointsEarned, booking.getUserId(), booking.getBookingCode());
                }
            } catch (Exception e) {
                log.error("Lỗi khi cộng điểm thưởng cho booking {}: {}", booking.getBookingCode(), e.getMessage());
            }

            // --- BẮT ĐẦU IN LOG KIỂM TRA NULL ---
            log.info("🔍 SOI DATA BOOKING:");
            log.info("- Booking Code: {}", booking.getBookingCode());
            log.info("- Guest Name: {}", booking.getGuestName()); // Đảm bảo dòng này in ra có tên không hay là null?
            log.info("- Homestay Name: {}", homestay != null ? homestay.getName() : "Homestay bị null");
            log.info("- Total Price: {}", booking.getTotalPrice());
            // ------------------------------------

            // 3. TẠO PAYLOAD BẰNG HASHMAP (CHỐNG NULL)
            String safeGuestName = booking.getGuestName() != null ? booking.getGuestName() : "Khách hàng";
            String safeHomestayName = (homestay != null && homestay.getName() != null) ? homestay.getName() : "Homestay";


            Map<String, Object> hostMetadata = new HashMap<>();
            hostMetadata.put("bookingId", booking.getId());
            hostMetadata.put("bookingCode", booking.getBookingCode());
            hostMetadata.put("homestayName", safeHomestayName);

            notificationService.sendNotification(
                    homestay.getOwnerId(),
                    NotificationType.BOOKING_CONFIRMED,
                    "Có đơn đặt phòng mới!",
                    String.format("🎉 Khách hàng %s vừa thanh toán đơn %s", safeGuestName, booking.getBookingCode()),
                    hostMetadata
            );

            // Gửi cho User (Khách)
            Map<String, Object> userMetadata = new HashMap<>();
            userMetadata.put("bookingId", booking.getId());
            userMetadata.put("homestayName", safeHomestayName);

            notificationService.sendNotification(
                    booking.getUserId(), // Giả sử trong Booking có userId
                    NotificationType.BOOKING_CONFIRMED,
                    "Thanh toán thành công",
                    String.format("Đơn đặt phòng %s tại %s đã được xác nhận.", booking.getBookingCode(), safeHomestayName),
                    userMetadata
            );

        } catch (Exception e) {
            log.error("❌ Lỗi nghiêm trọng khi chạy luồng Hậu thanh toán cho Booking: {}", booking.getBookingCode(), e);
        }
    }
}