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

import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        java.time.OffsetDateTime draftThreshold = java.time.OffsetDateTime.now().minus(15, ChronoUnit.MINUTES);
        java.time.OffsetDateTime paymentThreshold = java.time.OffsetDateTime.now().minus(30, ChronoUnit.MINUTES);

        List<Booking> expiredBookings = bookingRepository.findAllExpired(draftThreshold, paymentThreshold, BookingStatus.DRAFT, BookingStatus.AWAITING_PAYMENT);

        if (expiredBookings.isEmpty()) return;

        List<Long> expiredIds = expiredBookings.stream().map(Booking::getId).toList();
        log.info("[Clyvasync Lock] Bắt đầu giải phóng lô {} đơn hàng quá hạn.", expiredIds.size());

        bookingRepository.updateStatusByIds(expiredIds, BookingStatus.CANCELLED);

        List<BookingDetail> details = bookingDetailRepository.findByBookingIdIn(expiredIds);
        for (BookingDetail detail : details) {
            roomCalendarRepository.unlockRoomRange(
                    detail.getRoomId(),
                    detail.getCheckInDate(),
                    detail.getCheckOutDate(),
                    detail.getQuantity()
            );
        }


        List<TourBooking> draftTours = tourBookingRepository.findAllByHomestayBookingIdInAndStatus(expiredIds, TourBookingStatus.DRAFT);

        if (!draftTours.isEmpty()) {
            List<Long> tourBookingIds = draftTours.stream().map(TourBooking::getId).toList();

            tourBookingRepository.updateStatusByIds(tourBookingIds, TourBookingStatus.CANCELLED);


            Map<Long, Integer> slotsToReleaseMap = draftTours.stream()
                    .collect(Collectors.groupingBy(
                            TourBooking::getAvailabilityId,
                            Collectors.summingInt(TourBooking::getParticipantCount)
                    ));

            slotsToReleaseMap.forEach((availabilityId, totalSlots) -> {
                tourAvailabilityRepository.releaseTourSlots(availabilityId, totalSlots);
            });
        }

        log.info("[Clyvasync Lock] Hoàn tất dọn dẹp lô đơn quá hạn.");
    }
}