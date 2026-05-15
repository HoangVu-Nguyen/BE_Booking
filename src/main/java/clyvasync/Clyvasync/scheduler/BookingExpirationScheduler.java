package clyvasync.Clyvasync.scheduler;

import clyvasync.Clyvasync.enums.booking.BookingStatus;
import clyvasync.Clyvasync.enums.type.TourBookingStatus;
import clyvasync.Clyvasync.modules.booking.entity.Booking;
import clyvasync.Clyvasync.modules.booking.entity.BookingDetail;

import clyvasync.Clyvasync.modules.tour.entity.TourBooking;
import clyvasync.Clyvasync.repository.booking.BookingDetailRepository;
import clyvasync.Clyvasync.repository.booking.BookingRepository;
import clyvasync.Clyvasync.repository.room.RoomCalendarRepository;
import clyvasync.Clyvasync.repository.tour.TourAvailabilityRepository;
import clyvasync.Clyvasync.repository.tour.TourBookingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class BookingExpirationScheduler {

    private final BookingRepository bookingRepository;
    private final BookingDetailRepository bookingDetailRepository;
    private final RoomCalendarRepository roomCalendarRepository;
    private final TourBookingRepository tourBookingRepository;
    private final TourAvailabilityRepository tourAvailabilityRepository;

    @Scheduled(fixedDelay = 60000)
    @Transactional
    public void releaseExpiredBookings() {
        java.time.OffsetDateTime expirationThreshold = java.time.OffsetDateTime.now()
                .minus(15, java.time.temporal.ChronoUnit.MINUTES);

        List<Booking> expiredBookings = bookingRepository
                .findAllByStatusAndCreatedAtBefore(BookingStatus.DRAFT.name(), expirationThreshold);

        if (expiredBookings.isEmpty()) return;

        log.info("[Clyvasync Lock] Phát hiện {} đơn hàng nháp quá hạn 15 phút. Tiến hành giải phóng...", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            // 1. Huỷ Booking tổng
            booking.setStatus(BookingStatus.CANCELLED.name());
            bookingRepository.save(booking);

            // 2. Nhả phòng (Giữ nguyên logic cũ của bác)
            bookingDetailRepository.findBookingDetailByBookingId(booking.getId()).ifPresent(detail -> {
                roomCalendarRepository.unlockRoomRange(
                        detail.getRoomId(),
                        detail.getCheckInDate(),
                        detail.getCheckOutDate(),
                        detail.getQuantity()
                );
            });

            // 3. XOÁ FULL DANH SÁCH TOUR ĐI KÈM
            // Chỗ này mình dùng findAllBy... thay vì findBy...
            List<TourBooking> tourBookings = tourBookingRepository.findAllByHomestayBookingId(booking.getId());

            for (TourBooking tourBooking : tourBookings) {
                if (TourBookingStatus.DRAFT.equals(tourBooking.getStatus())) {
                    // Đổi trạng thái tour từng cái
                    tourBooking.setStatus(TourBookingStatus.CANCELLED);
                    tourBookingRepository.save(tourBooking);

                    // Cộng lại slot cho từng tour tương ứng
                    tourAvailabilityRepository.releaseTourSlots(
                            tourBooking.getAvailabilityId(),
                            tourBooking.getParticipantCount()
                    );
                    log.info("[Clyvasync Lock] Đã nhả slot cho Tour ID: {}", tourBooking.getTourId());
                }
            }
        }
        log.info("[Clyvasync Lock] Đã hoàn tất dọn dẹp toàn bộ phòng và tour cho các đơn quá hạn.");
    }
}
