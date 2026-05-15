package clyvasync.Clyvasync.service.booking.impl;

import clyvasync.Clyvasync.constant.ImageConstants;
import clyvasync.Clyvasync.dto.detail.PolicyDetail;
import clyvasync.Clyvasync.dto.detail.TourBookingItemDetail;
import clyvasync.Clyvasync.dto.detail.TourDetail;
import clyvasync.Clyvasync.dto.request.BookingInitRequest;
import clyvasync.Clyvasync.dto.response.BookingDetailsResponse;
import clyvasync.Clyvasync.dto.response.BookingInitResponse;
import clyvasync.Clyvasync.dto.response.HomestayResponse;
import clyvasync.Clyvasync.dto.response.RoomResponse;
import clyvasync.Clyvasync.enums.booking.BookingStatus;
import clyvasync.Clyvasync.enums.type.PaymentStatus;
import clyvasync.Clyvasync.enums.type.TourBookingStatus;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.booking.entity.Booking;
import clyvasync.Clyvasync.modules.booking.entity.BookingDetail;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayPolicy;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayRoom;
import clyvasync.Clyvasync.modules.room.RoomRatePlan;
import clyvasync.Clyvasync.modules.tour.entity.Tour;
import clyvasync.Clyvasync.modules.tour.entity.TourBooking;
import clyvasync.Clyvasync.modules.tour.entity.TourImage;
import clyvasync.Clyvasync.repository.booking.BookingRepository;
import clyvasync.Clyvasync.service.booking.BookingDetailService;
import clyvasync.Clyvasync.service.booking.BookingService;
import clyvasync.Clyvasync.service.homestay.HomestayPolicyService;
import clyvasync.Clyvasync.service.homestay.HomestayRoomService;
import clyvasync.Clyvasync.service.homestay.HomestayService;
import clyvasync.Clyvasync.service.room.RoomCalendarService;
import clyvasync.Clyvasync.service.room.RoomRatePlanService;
import clyvasync.Clyvasync.service.tour.TourAvailabilityService;
import clyvasync.Clyvasync.service.tour.TourBookingService;
import clyvasync.Clyvasync.service.tour.TourImageService;
import clyvasync.Clyvasync.service.tour.TourService;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {
    private final BookingRepository bookingRepository;
    private final HomestayRoomService roomService;
    private final BookingDetailService bookingDetailService;
    private final RoomRatePlanService roomRatePlanService;
    private final TourService tourService;

    private final TourBookingService tourBookingService;
    private final HomestayService homestayService;
    private final TourImageService tourImageService;
    private final HomestayPolicyService homestayPolicyService;
    private final RoomCalendarService roomCalendarService;
    private final TourAvailabilityService tourAvailabilityService;

    @Override
    public boolean existsActiveBooking(Long userId, Long homestayId) {
        return true;
    }

    @Override
    public List<LocalDate> getUnavailableDates(Long roomId, int month, int year) {
        // 1. Xác định ngày đầu và ngày cuối của tháng
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();

        // 2. Lấy thông tin phòng để biết tổng số lượng căn (quantity)
        HomestayRoom room = roomService.getRoomById(roomId);
        int totalRoomQuantity = room.getQuantity();

        // 3. Lấy tất cả các BookingDetail trùng với tháng này
        List<BookingDetail> overlappingBookings = bookingDetailService.findOverlappingBookings(roomId, startOfMonth, endOfMonth);

        // 4. Tính toán số phòng đã được đặt cho TỪNG NGÀY trong tháng
        Map<LocalDate, Integer> dailyBookedMap = new HashMap<>();

        for (BookingDetail detail : overlappingBookings) {
            LocalDate current = detail.getCheckInDate();
            LocalDate end = detail.getCheckOutDate();

            // Duyệt từ checkInDate đến TRƯỚC checkOutDate (Khách trả phòng thì hôm đó vẫn tính là trống cho khách mới)
            while (current.isBefore(end)) {
                // Chỉ đếm những ngày nằm trong tháng đang xét
                if (!current.isBefore(startOfMonth) && !current.isAfter(endOfMonth)) {
                    dailyBookedMap.put(current, dailyBookedMap.getOrDefault(current, 0) + detail.getQuantity());
                }
                current = current.plusDays(1);
            }
        }

        // 5. Lọc ra những ngày đã FULL phòng (số phòng đã đặt >= tổng số phòng có sẵn)
        List<LocalDate> unavailableDates = new ArrayList<>();
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            int bookedQty = dailyBookedMap.getOrDefault(date, 0);

            if (bookedQty >= totalRoomQuantity) {
                unavailableDates.add(date);
            }
        }

        return unavailableDates;
    }

    @Override
    @Transactional
    public BookingInitResponse initBooking(BookingInitRequest request, Long userId) {
        System.out.println(request);

        // 1. TÍNH SỐ ĐÊM & KHÓA PHÒNG (Giữ nguyên)
        long nights = java.time.temporal.ChronoUnit.DAYS.between(request.getCheckInDate(), request.getCheckOutDate());
        if (nights <= 0) throw new AppException(ResultCode.INVALID_DATE_RANGE);

        int roomRowsUpdated = roomCalendarService.lockRoomRange(
                request.getRoomId(),
                request.getCheckInDate(),
                request.getCheckOutDate(),
                request.getRoomQuantity()
        );
        if (roomRowsUpdated != nights) throw new AppException(ResultCode.ROOM_NOT_AVAILABLE);

        // 2. KHÓA SLOT CHO TOÀN BỘ DANH SÁCH TOUR
        BigDecimal totalTourPrice = BigDecimal.ZERO;

        // Check nếu khách có chọn tour
        if (request.getTours() != null && !request.getTours().isEmpty()) {
            for (TourBookingItemDetail tourItem : request.getTours()) {
                int tourRowsUpdated = tourAvailabilityService.deductTourSlots(
                        tourItem.getAvailabilityId(),
                        tourItem.getParticipantCount()
                );

                // Nếu bất kỳ tour nào hết chỗ -> Rollback toàn bộ (kể cả phòng)
                if (tourRowsUpdated == 0) {
                    throw new AppException(ResultCode.TOUR_NOT_AVAILABLE);
                }

                // Tính giá cho từng tour để cộng dồn
                Tour tour = tourService.findTourById(tourItem.getTourId());
                BigDecimal itemTotal = tour.getPricePerPerson().multiply(BigDecimal.valueOf(tourItem.getParticipantCount()));
                totalTourPrice = totalTourPrice.add(itemTotal);
            }
        }

        // 3. TÍNH TOÁN TỔNG TIỀN HÓA ĐƠN
        String bookingCode = "BK-" + System.currentTimeMillis() % 1000000 + "-" + generateRandomString();
        RoomRatePlan ratePlan = roomRatePlanService.getById(request.getRatePlanId());

        BigDecimal roomSubtotal = ratePlan.getPrice()
                .multiply(BigDecimal.valueOf(nights))
                .multiply(BigDecimal.valueOf(request.getRoomQuantity()));

        BigDecimal totalPrice = roomSubtotal.add(totalTourPrice);
        BigDecimal taxFee = totalPrice.multiply(new BigDecimal("0.10"));
        BigDecimal finalGrandTotal = totalPrice.add(taxFee);
        int expectedPoints = finalGrandTotal.multiply(new BigDecimal("0.01")).intValue();

        // 4. LƯU BOOKING TỔNG
        Booking booking = Booking.builder()
                .bookingCode(bookingCode)
                .userId(userId)
                .homestayId(request.getHomestayId())
                .totalPrice(finalGrandTotal)
                .taxFee(taxFee)
                .status(BookingStatus.DRAFT.name())
                .paymentStatus(PaymentStatus.UNPAID.name())
                .loyaltyPointsEarned(expectedPoints)
                .guestName(request.getGuestName())
                .guestEmail(request.getEmail())
                .guestPhone(request.getPhone())
                .specialRequests(request.getSpecialRequests())
                .build();

        booking = bookingRepository.save(booking);

        // 5. LƯU CHI TIẾT PHÒNG
        BookingDetail detail = BookingDetail.builder()
                .bookingId(booking.getId())
                .roomId(request.getRoomId())
                .ratePlanId(request.getRatePlanId())
                .checkInDate(request.getCheckInDate())
                .checkOutDate(request.getCheckOutDate())
                .quantity(request.getRoomQuantity())
                .guestCount(request.getGuestCount())
                .unitPrice(ratePlan.getPrice())
                .subtotal(roomSubtotal)
                .build();
        bookingDetailService.save(detail);

        // 6. LƯU CHI TIẾT TỪNG TOUR VÀO BẢNG TOUR_BOOKINGS
        if (request.getTours() != null) {
            for (TourBookingItemDetail tourItem : request.getTours()) {
                Tour tour = tourService.findTourById(tourItem.getTourId());
                BigDecimal itemTotal = tour.getPricePerPerson().multiply(BigDecimal.valueOf(tourItem.getParticipantCount()));

                TourBooking tourBooking = TourBooking.builder()
                        .bookingCode("TR-" + generateRandomString()) // Code riêng cho từng tour
                        .tourId(tourItem.getTourId())
                        .userId(userId)
                        .homestayBookingId(booking.getId())
                        .availabilityId(tourItem.getAvailabilityId())
                        .tourDate(tourItem.getTourDate())
                        .participantCount(tourItem.getParticipantCount())
                        .totalPrice(itemTotal)
                        .status(TourBookingStatus.DRAFT)
                        .paymentStatus(PaymentStatus.UNPAID)
                        .build();

                tourBookingService.save(tourBooking);
            }
        }

        return new BookingInitResponse(bookingCode, booking.getId());
    }

    @Override
    public BookingDetailsResponse getBookingDetailsByCode(String bookingCode) {
        Booking booking = bookingRepository.findBookingByBookingCode(bookingCode)
                .orElseThrow(() -> new AppException(ResultCode.BOOKING_NOT_FOUND));

        BookingDetail detail = bookingDetailService.findBookingDetailByBookingId(booking.getId());
        HomestayResponse homestayResponse = homestayService.getById(booking.getHomestayId());
        HomestayRoom homestayRoom = roomService.getRoomById(detail.getRoomId());
        HomestayPolicy homestayPolicy = homestayPolicyService.getHomestayPolicyByHomestayId(booking.getHomestayId());

        List<TourBooking> tourBookings = tourBookingService.findAllByHomestayBookingId(booking.getId());
        List<TourDetail> tourDetails = List.of();
        BigDecimal tourSubtotal = BigDecimal.ZERO;

        if (!tourBookings.isEmpty()) {
            // GOM TẤT CẢ ID TOUR LẠI (Batching)
            List<Long> tourIds = tourBookings.stream().map(TourBooking::getTourId).distinct().toList();

            // CHỈ 1 QUERY lấy toàn bộ thông tin Tour lõi lên Map (Map<Id, Tour>)
            Map<Long, Tour> tourMap = tourService.findAllByIds(tourIds).stream()
                    .collect(Collectors.toMap(Tour::getId, t -> t));

            // CHỈ 1 QUERY lấy toàn bộ ảnh đại diện Tour lên Map (Map<TourId, ImageUrl>)
            Map<Long, String> tourImageMap = tourImageService.getPrimaryImagesByTourIds(tourIds);

            // Map sang DTO từ bộ nhớ (In-memory mapping - Không đụng vào DB nữa)
            tourDetails = tourBookings.stream().map(tb -> {
                Tour tourCore = tourMap.get(tb.getTourId());
                return TourDetail.builder()
                        .tourBookingId(tb.getId())
                        .tourBookingCode(tb.getBookingCode())
                        .tourName(tourCore != null ? tourCore.getName() : "N/A")
                        .tourImage(tourImageMap.getOrDefault(tb.getTourId(), ImageConstants.TOUR_DEFAULT))
                        .tourDate(tb.getTourDate())
                        .participantCount(tb.getParticipantCount())
                        .totalPrice(tb.getTotalPrice())
                        .build();
            }).toList();

            // Tính tổng tiền Tour từ mảng đã lấy
            tourSubtotal = tourDetails.stream()
                    .map(TourDetail::getTotalPrice)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
        }

        // 4. ĐÓNG GÓI RESPONSE
        long totalNights = java.time.temporal.ChronoUnit.DAYS.between(detail.getCheckInDate(), detail.getCheckOutDate());

        return BookingDetailsResponse.builder()
                .bookingId(booking.getId())
                .bookingCode(booking.getBookingCode())
                .status(booking.getStatus())
                .paymentStatus(booking.getPaymentStatus())
                .specialRequests(booking.getSpecialRequests())
                .loyaltyPointsEarned(booking.getLoyaltyPointsEarned())
                .homestayId(homestayResponse.getId())
                .homestayName(homestayResponse.getName())
                .homestayAddress(homestayResponse.getAddressDetail())
                .roomName(homestayRoom.getName())
                .roomImage(homestayRoom.getImageUrl())

                .checkInDate(detail.getCheckInDate())
                .checkOutDate(detail.getCheckOutDate())
                .totalNights(totalNights)
                .roomQuantity(detail.getQuantity())
                .guestCount(detail.getGuestCount())

                .tours(tourDetails) // Trả về mảng Tour
                .policy(mapToPolicyDto(homestayPolicy))

                .roomSubtotal(detail.getSubtotal())
                .tourSubtotal(tourSubtotal)
                .taxFee(booking.getTaxFee())
                .grandTotal(booking.getTotalPrice())
                .build();
    }

    @Override
    public Booking getBookingByCode(String bookingCode) {
        return bookingRepository.findBookingByBookingCode(bookingCode).orElseThrow(() -> new AppException(ResultCode.BOOKING_NOT_FOUND));
    }

    // Hàm phụ để code nhìn gọn hơn
    private PolicyDetail mapToPolicyDto(HomestayPolicy policy) {
        return PolicyDetail.builder()
                .checkInTime(policy.getCheckInTime())
                .checkOutTime(policy.getCheckOutTime())
                .lateCheckInInstruction(policy.getLateCheckInInstruction())
                .allowsPets(policy.getAllowsPets())
                .allowsSmoking(policy.getAllowsSmoking())
                .allowsParties(policy.getAllowsParties())
                .build();
    }

    private String generateRandomString() {
        return java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }
}
