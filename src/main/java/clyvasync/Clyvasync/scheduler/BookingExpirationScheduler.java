package clyvasync.Clyvasync.scheduler;

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
        // Mốc thời gian tối đa: Hiện tại trừ đi 15 phút
        java.time.OffsetDateTime expirationThreshold = java.time.OffsetDateTime.now()
                .minus(15, java.time.temporal.ChronoUnit.MINUTES);

        List<Booking> expiredBookings = bookingRepository
                .findAllByStatusAndCreatedAtBefore("DRAFT", expirationThreshold);

        if (expiredBookings.isEmpty()) return;

        log.info("[Clyvasync Lock] Phát hiện {} đơn hàng nháp quá hạn 15 phút. Tiến hành giải phóng...", expiredBookings.size());

        for (Booking booking : expiredBookings) {
            // 1. Chuyển trạng thái sang CANCELLED
            booking.setStatus("CANCELLED");
            bookingRepository.save(booking);

            bookingDetailRepository.findById(booking.getId()).ifPresent(detail -> {
                roomCalendarRepository.unlockRoomRange(
                        detail.getRoomId(),
                        detail.getCheckInDate(),
                        detail.getCheckOutDate(),
                        detail.getQuantity()
                );
            });

            tourBookingRepository.findByHomestayBookingId(booking.getId()).ifPresent(tourBooking -> {
                if (TourBookingStatus.DRAFT.equals(tourBooking.getStatus())) {
                    tourBooking.setStatus(TourBookingStatus.CANCELLED);
                    tourBookingRepository.save(tourBooking);

                    tourAvailabilityRepository.releaseTourSlots(
                            tourBooking.getAvailabilityId(),
                            tourBooking.getParticipantCount()
                    );
                }
            });
        }
        log.info("[Clyvasync Lock] Đã dọn dẹp sạch sẽ tài nguyên đơn quá hạn.");
    }
}
