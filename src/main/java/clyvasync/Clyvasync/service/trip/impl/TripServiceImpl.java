package clyvasync.Clyvasync.service.trip.impl;

import clyvasync.Clyvasync.constant.ImageConstants;
import clyvasync.Clyvasync.dto.response.TripResponse;
import clyvasync.Clyvasync.dto.response.TripTourResponse;
import clyvasync.Clyvasync.enums.booking.BookingStatus;
import clyvasync.Clyvasync.enums.type.PaymentStatus;
import clyvasync.Clyvasync.modules.booking.entity.Booking;
import clyvasync.Clyvasync.modules.booking.entity.BookingDetail;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import clyvasync.Clyvasync.modules.tour.entity.Tour;
import clyvasync.Clyvasync.modules.tour.entity.TourAvailability;
import clyvasync.Clyvasync.modules.tour.entity.TourBooking;
import clyvasync.Clyvasync.service.booking.BookingDetailService;
import clyvasync.Clyvasync.service.booking.BookingService;
import clyvasync.Clyvasync.service.homestay.HomestayImageService;
import clyvasync.Clyvasync.service.homestay.HomestayService;
import clyvasync.Clyvasync.service.tour.TourAvailabilityService;
import clyvasync.Clyvasync.service.tour.TourBookingService;
import clyvasync.Clyvasync.service.tour.TourImageService;
import clyvasync.Clyvasync.service.tour.TourService;
import clyvasync.Clyvasync.service.trip.TripService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class TripServiceImpl implements TripService {
    private final BookingService bookingService;
    private final HomestayService homestayService;
    private final BookingDetailService bookingDetailService;
    private final TourBookingService tourBookingService;
    private final TourService tourService;
    private final TourImageService tourImageService;
    private final TourAvailabilityService tourAvailabilityService;
    private final HomestayImageService homestayImageService;


    @Override
    public List<TripResponse> getUserTrips(Long userId) {
        log.info("[TRIP SERVICE] Bốc danh sách hành trình cho user ID (ID-only mode): {}", userId);

        List<Booking> bookings = bookingService.findByUserIdOrderByCreatedAtDesc(userId);
        if (bookings.isEmpty()) {
            return Collections.emptyList();
        }
        List<Long> bookingIds = bookings.stream().map(Booking::getId).collect(Collectors.toList());
        List<Long> homestayIds = bookings.stream().map(Booking::getHomestayId).distinct().collect(Collectors.toList());
        Map<Long, Homestay> homestayMap = homestayService.findByIdIn(homestayIds).stream()
                .collect(Collectors.toMap(Homestay::getId, h -> h));

        // Map<BookingId, List<BookingDetail>>
        Map<Long, List<BookingDetail>> detailsMap = bookingDetailService.findByBookingIdIn(bookingIds).stream()
                .collect(Collectors.groupingBy(BookingDetail::getBookingId));
        Map<Long, List<TourBooking>> tourBookingsMap = tourBookingService.findByHomestayBookingIdIn(bookingIds).stream()
                .collect(Collectors.groupingBy(TourBooking::getHomestayBookingId));
        List<Long> tourIds = tourBookingsMap.values().stream()
                .flatMap(List::stream)
                .map(TourBooking::getTourId)
                .distinct()
                .collect(Collectors.toList());

        Map<Long, Tour> tourMap = tourIds.isEmpty() ? Collections.emptyMap() :
                tourService.findByIdIn(tourIds).stream()
                        .collect(Collectors.toMap(Tour::getId, t -> t));
        Map<Long,String> tourImageMap = tourImageService.getPrimaryImagesByTourIds(tourIds);
        List<Long> availabilityIds = tourBookingsMap.values().stream()
                .flatMap(List::stream)
                .map(TourBooking::getAvailabilityId) // Lấy availability_id từ tour_bookings
                .distinct()
                .collect(Collectors.toList());
        Map<Long, TourAvailability> availabilityMap = availabilityIds.isEmpty() ? Collections.emptyMap() :
                tourAvailabilityService.findByIdIn(availabilityIds).stream()
                        .collect(Collectors.toMap(TourAvailability::getId, a -> a));
        Map<Long,List<String>> homestayImagesMap = homestayImageService.getImagesForHomestays(homestayIds);
        LocalDate today = LocalDate.now();
        return bookings.stream().map(booking -> {
            Homestay homestay = homestayMap.get(booking.getHomestayId());
            List<BookingDetail> details = detailsMap.getOrDefault(booking.getId(), Collections.emptyList());
            List<TourBooking> tourBookings = tourBookingsMap.getOrDefault(booking.getId(), Collections.emptyList());

            LocalDate minCheckIn = details.stream()
                    .map(BookingDetail::getCheckInDate)
                    .min(LocalDate::compareTo)
                    .orElse(today);

            LocalDate maxCheckOut = details.stream()
                    .map(BookingDetail::getCheckOutDate)
                    .max(LocalDate::compareTo)
                    .orElse(today.plusDays(1));

            int totalGuests = details.stream().mapToInt(BookingDetail::getGuestCount).sum();

            String tripStatus;

            if ("CANCELLED".equalsIgnoreCase(booking.getStatus())) {
                tripStatus = "CANCELLED";
            }
            else if (PaymentStatus.UNPAID.name().equalsIgnoreCase(booking.getPaymentStatus())
                    && BookingStatus.DRAFT.name().equalsIgnoreCase(booking.getStatus())) {
                // Bác nhớ thêm .name() hoặc ép kiểu chuẩn chuỗi nếu b.getStatus() là Enum nhé
                tripStatus = "PENDING";
            }
            else {
                if (today.isAfter(minCheckIn) || today.isEqual(minCheckIn)) {
                    tripStatus = "COMPLETED"; // Đang ở hoặc đã ở xong
                } else {
                    tripStatus = "UPCOMING";  // Sắp khởi hành
                }
            }

            // ==========================================
            // 3. LOGIC MỚI: RÁP START TIME VÀ END TIME
            // ==========================================
            List<TripTourResponse> tourResponses = tourBookings.stream().map(tb -> {
                Tour tour = tourMap.get(tb.getTourId());
                TourAvailability availability = availabilityMap.get(tb.getAvailabilityId());

                LocalTime startTime = availability != null ? availability.getStartTime() : null;
                LocalTime endTime = null;

                // Tính toán End Time dựa trên Duration Type của bảng Tour
                if (startTime != null && tour != null) {
                    if ("HOURS".equalsIgnoreCase(tour.getDurationType()) && tour.getDurationValue() != null) {
                        endTime = startTime.plusHours(tour.getDurationValue());
                    }
                    // Nếu là DAYS thì tour kéo dài qua ngày, trên UI cái timeline giờ trong ngày
                    // thường chỉ hiện giờ xuất phát, nên ta để null hoặc bằng startTime tùy ý bác.
                }

                return TripTourResponse.builder()
                        .tourId(tour != null ? tour.getId().toString() : tb.getTourId().toString())
                        .tourName(tour != null ? tour.getName() : "Tour không xác định")
                        .tourImage(tourImageMap.get(tour.getId()))
                        .participants(tb.getParticipantCount())
                        .tourDate(tb.getTourDate()) // Từ DB tour_bookings
                        .startTime(startTime)       // Từ DB tour_availability
                        .endTime(endTime)           // Đã cộng giờ tự động
                        .build();
            }).collect(Collectors.toList());
            List<String> images = homestayImagesMap.getOrDefault(booking.getHomestayId(), Collections.emptyList());

            String coverImage = images.isEmpty() ? ImageConstants.TOUR_DEFAULT : images.get(0);

            return TripResponse.builder()
                    .bookingCode(booking.getBookingCode())
                    .propertyName(homestay != null ? homestay.getName() : "Đang cập nhật")
                    .location(homestay != null ? homestay.getAddressDetail() : "")
                    .propertyImage(coverImage)
                    .checkIn(minCheckIn)
                    .checkOut(maxCheckOut)
                    .totalGuests(totalGuests)
                    .totalPrice(booking.getTotalPrice())
                    .status(tripStatus)
                    .tours(tourResponses)
                    .build();

        }).collect(Collectors.toList());
    }
}
