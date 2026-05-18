package clyvasync.Clyvasync.service.trip.impl;

import clyvasync.Clyvasync.constant.ImageConstants;
import clyvasync.Clyvasync.dto.detail.PropertyDetailInfo;
import clyvasync.Clyvasync.dto.detail.RoomBookedInfo;
import clyvasync.Clyvasync.dto.detail.TourTimelineInfo;
import clyvasync.Clyvasync.dto.response.*;
import clyvasync.Clyvasync.enums.booking.BookingStatus;
import clyvasync.Clyvasync.enums.type.PaymentStatus;
import clyvasync.Clyvasync.modules.booking.entity.Booking;
import clyvasync.Clyvasync.modules.booking.entity.BookingDetail;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayImage;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayPolicy;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayRoom;
import clyvasync.Clyvasync.modules.tour.entity.Tour;
import clyvasync.Clyvasync.modules.tour.entity.TourAvailability;
import clyvasync.Clyvasync.modules.tour.entity.TourBooking;
import clyvasync.Clyvasync.modules.tour.entity.TourImage;
import clyvasync.Clyvasync.service.auth.UserService;
import clyvasync.Clyvasync.service.booking.BookingDetailService;
import clyvasync.Clyvasync.service.booking.BookingService;
import clyvasync.Clyvasync.service.homestay.HomestayImageService;
import clyvasync.Clyvasync.service.homestay.HomestayPolicyService;
import clyvasync.Clyvasync.service.homestay.HomestayRoomService;
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
import java.time.format.DateTimeFormatter;
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
    private final UserService userService;
    private final HomestayRoomService homestayRoomService;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
    private final HomestayPolicyService homestayPolicyService;


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

    @Override
    public TripDetailResponse getTripDetail(String bookingCode, Long currentUserId) {
        log.info("[TRIP DETAIL] Đang tải chi tiết cho mã: {}", bookingCode);

        // 1. Lấy Booking gốc (Có check quyền sở hữu user)
        Booking booking = bookingService.findByBookingCodeAndUserId(bookingCode, currentUserId);
        List<BookingDetail> bookingDetails = bookingDetailService.findAllByBookingId(booking.getId());
        LocalDate today = LocalDate.now();
        LocalDate minCheckIn = bookingDetails.stream().map(BookingDetail::getCheckInDate).min(LocalDate::compareTo).orElse(today);
        LocalDate maxCheckOut = bookingDetails.stream().map(BookingDetail::getCheckOutDate).max(LocalDate::compareTo).orElse(today.plusDays(1));
        int totalGuests = bookingDetails.stream().mapToInt(BookingDetail::getGuestCount).sum();
        String tripStatus = "UPCOMING";
        if (BookingStatus.CANCELLED.name().equalsIgnoreCase(booking.getStatus())) {
            tripStatus = "CANCELLED";
        } else if (PaymentStatus.UNPAID.name().equalsIgnoreCase(booking.getPaymentStatus()) && PaymentStatus.PAID.name().equalsIgnoreCase(booking.getStatus())) { // Sửa lại enum status cho khớp DB bác nhé
            tripStatus = "PENDING";
        } else {
            if (today.isAfter(minCheckIn) || today.isEqual(minCheckIn)) {
                tripStatus = "COMPLETED";
            }
        }
        HomestayResponse homestay = homestayService.getById(booking.getHomestayId());
        HomestayPolicy policy = homestayPolicyService.getHomestayPolicyByHomestayId(booking.getHomestayId());
        BookingPolicyResponse policyResponse = null;
        if (policy != null) {
            policyResponse = BookingPolicyResponse.builder()
                    .checkInTime(policy.getCheckInTime() != null ? policy.getCheckInTime().toString().substring(0, 5) : "14:00")
                    .checkOutTime(policy.getCheckOutTime() != null ? policy.getCheckOutTime().toString().substring(0, 5) : "12:00")
                    .allowsPets(policy.getAllowsPets() != null ? policy.getAllowsPets() : false)
                    .allowsSmoking(policy.getAllowsSmoking() != null ? policy.getAllowsSmoking() : false)
                    .allowsParties(policy.getAllowsParties() != null ? policy.getAllowsParties() : false)
                    .build();
        } else {
            policyResponse = BookingPolicyResponse.builder()
                    .checkInTime("14:00").checkOutTime("12:00")
                    .allowsPets(false).allowsSmoking(false).allowsParties(false)
                    .build();
        }
        List<String> propertyImages = homestayImageService.findByHomestayId(booking.getHomestayId()).stream()
                .sorted((a, b) -> Integer.compare(a.getDisplayOrder(), b.getDisplayOrder()))
                .map(HomestayImage::getImageUrl)
                .toList();
        PropertyDetailInfo propertyInfo = PropertyDetailInfo.builder()
                .name(homestay != null ? homestay.getName() : "Không xác định")
                .address(homestay != null ? homestay.getAddressDetail() : "")
                .latitude(homestay != null ? homestay.getLatitude() : null)
                .longitude(homestay != null ? homestay.getLongitude() : null)
                .images(propertyImages)
                .build();

        OwnerResponse ownerResponse = userService.getOwnerInfo(booking.getUserId());
        List<Long> roomIds = bookingDetails.stream().map(BookingDetail::getRoomId).distinct().toList();
        Map<Long, HomestayRoom> roomMap = homestayRoomService.findByIdIn(roomIds).stream()
                .collect(Collectors.toMap(HomestayRoom::getId, r -> r));
        List<RoomBookedInfo> roomsBooked = bookingDetails.stream().map(detail -> {
            HomestayRoom room = roomMap.get(detail.getRoomId());
            return RoomBookedInfo.builder()
                    .roomName(room != null ? room.getName() : "Phòng tiêu chuẩn")
                    .roomTag(room != null ? room.getTag() : "")
                    .quantity(detail.getQuantity())
                    .guests(detail.getGuestCount())
                    .build();
        }).collect(Collectors.toList());
        List<TourBooking> tourBookings = tourBookingService.findAllByHomestayBookingId(booking.getId());

        List<TourTimelineInfo> toursBooked = Collections.emptyList();
        if (!tourBookings.isEmpty()) {
            List<Long> tourIds = tourBookings.stream().map(TourBooking::getTourId).distinct().collect(Collectors.toList());
            List<Long> availabilityIds = tourBookings.stream().map(TourBooking::getAvailabilityId).distinct().collect(Collectors.toList());

            // Tra cứu Map tốc độ cao
            Map<Long, Tour> tourMap = tourService.findByIdIn(tourIds).stream().collect(Collectors.toMap(Tour::getId, t -> t));
            Map<Long, TourAvailability> availabilityMap = tourAvailabilityService.findByIdIn(availabilityIds).stream().collect(Collectors.toMap(TourAvailability::getId, a -> a));

            // Map ảnh Tour (Chỉ lấy ảnh chính isPrimary = true)
            Map<Long, String> tourImagesMap = tourImageService.findByTourIdIn(tourIds).stream()
                    .filter(img -> Boolean.TRUE.equals(img.getIsPrimary()))
                    .collect(Collectors.toMap(TourImage::getTourId, TourImage::getImageUrl, (img1, img2) -> img1)); // Đề phòng 1 tour có 2 ảnh chính thì lấy cái đầu

            toursBooked = tourBookings.stream().map(tb -> {
                Tour tour = tourMap.get(tb.getTourId());
                TourAvailability availability = availabilityMap.get(tb.getAvailabilityId());

                LocalTime startLocalTime = availability != null ? availability.getStartTime() : null;
                LocalTime endLocalTime = null;

                if (startLocalTime != null && tour != null && "HOURS".equalsIgnoreCase(tour.getDurationType())) {
                    endLocalTime = startLocalTime.plusHours(tour.getDurationValue());
                }

                return TourTimelineInfo.builder()
                        .tourId(tb.getTourId().toString())
                        .tourName(tour != null ? tour.getName() : "Trải nghiệm độc bản")
                        .tourImage(tourImagesMap.getOrDefault(tb.getTourId(), "https://default-tour.png"))
                        .tourDate(tb.getTourDate())
                        .startTime(startLocalTime != null ? startLocalTime.format(timeFormatter) : null) // Ép ra String "08:00" luôn
                        .endTime(endLocalTime != null ? endLocalTime.format(timeFormatter) : null)       // Ép ra String "16:00" luôn
                        .participants(tb.getParticipantCount())
                        .build();
            }).collect(Collectors.toList());
        }

        // 6. Ráp DTO Tổng và trả về
        return TripDetailResponse.builder()
                .bookingCode(booking.getBookingCode())
                .status(tripStatus)
                .paymentStatus(booking.getPaymentStatus())
                .checkIn(minCheckIn)
                .checkOut(maxCheckOut)
                .totalGuests(totalGuests)
                .totalPrice(booking.getTotalPrice())
                .paymentMethod("Chuyển khoản / Cổng thanh toán")
                .property(propertyInfo)
                .host(ownerResponse)
                .policy(policyResponse)
                .rooms(roomsBooked)
                .tours(toursBooked)
                .build();
    }
}

