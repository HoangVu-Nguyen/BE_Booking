package clyvasync.Clyvasync.service.room.impl;

import clyvasync.Clyvasync.dto.detail.BookingSimpleInfo;
import clyvasync.Clyvasync.dto.projection.BookingCalendarProjection;
import clyvasync.Clyvasync.dto.request.BatchUpdateCalendarRequest;
import clyvasync.Clyvasync.dto.response.CalendarInventoryResponse;
import clyvasync.Clyvasync.dto.response.CalendarRoomResponse;
import clyvasync.Clyvasync.dto.response.HomestayCalendarResponse;
import clyvasync.Clyvasync.dto.response.OwnerResponse;
import clyvasync.Clyvasync.enums.calendar.RoomCalendarStatus;
import clyvasync.Clyvasync.enums.homestay.HomestayStatus;
import clyvasync.Clyvasync.enums.room.RoomStatus;
import clyvasync.Clyvasync.modules.booking.entity.BookingDetail;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayRoom;
import clyvasync.Clyvasync.modules.room.RoomCalendar;
import clyvasync.Clyvasync.modules.room.RoomRatePlan;
import clyvasync.Clyvasync.repository.room.RoomCalendarRepository;
import clyvasync.Clyvasync.service.auth.UserService;
import clyvasync.Clyvasync.service.booking.BookingDetailService;
import clyvasync.Clyvasync.service.homestay.HomestayRoomService;
import clyvasync.Clyvasync.service.homestay.HomestayService;
import clyvasync.Clyvasync.service.room.RoomCalendarService;
import clyvasync.Clyvasync.service.room.RoomRatePlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service

@Slf4j
public class RoomCalendarServiceImpl implements RoomCalendarService {
    private final RoomCalendarRepository roomCalendarRepository;
    private final HomestayRoomService homestayRoomService;
    private final BookingDetailService bookingDetailService;
    private final RoomRatePlanService roomRatePlanService;
    private final HomestayService homestayService;
    private final UserService userService;

    public RoomCalendarServiceImpl(RoomCalendarRepository roomCalendarRepository, HomestayRoomService homestayRoomService, BookingDetailService bookingDetailService, RoomRatePlanService roomRatePlanService,@Lazy HomestayService homestayService,UserService userService) {
        this.roomCalendarRepository = roomCalendarRepository;
        this.homestayRoomService = homestayRoomService;
        this.bookingDetailService = bookingDetailService;
        this.roomRatePlanService = roomRatePlanService;
        this.homestayService = homestayService;
        this.userService = userService;
    }

    @Override
    public int lockRoomRange(Long roomId, LocalDate checkIn, LocalDate checkOut, int qty) {
        return roomCalendarRepository.lockRoomRange(roomId, checkIn, checkOut, qty);
    }

    @Override
    public int unlockRoomRange(Long roomId, LocalDate checkIn, LocalDate checkOut, int qty) {
        return roomCalendarRepository.unlockRoomRange(roomId, checkIn, checkOut, qty);
    }

    @Override
    public List<LocalDate> getUnavailableDates(Long roomId, int month, int year) {
        java.time.YearMonth yearMonth = java.time.YearMonth.of(year, month);
        LocalDate startOfMonth = yearMonth.atDay(1);
        LocalDate endOfMonth = yearMonth.atEndOfMonth();
        return roomCalendarRepository.findUnavailableDates(roomId, startOfMonth, endOfMonth);
    }

    @Override
    public List<RoomCalendar> findCalendarsByRoomIdsAndDateRange(List<Long> roomIds, LocalDate startDate, LocalDate endDate) {
        return roomCalendarRepository.findCalendarsByRoomIdsAndDateRange(roomIds, startDate, endDate);
    }

    @Override
    public List<RoomCalendar> findCustomCalendarByRoomIdsAndDateRange(List<Long> roomIds, LocalDate startDate, LocalDate endDate) {
        return roomCalendarRepository.findCustomCalendarByRoomIdsAndDateRange(roomIds, startDate, endDate);
    }

    @Override
    public HomestayCalendarResponse getHomestayCalendar(Long ownerId,Long homestayId, LocalDate startDate, LocalDate endDate) {
        OwnerResponse ownerResponse = userService.getOwnerInfo(ownerId);
        List<HomestayRoom> rooms = homestayRoomService.findAllByHomestayIdAndStatus(homestayId, RoomStatus.ACTIVE);
        Homestay homestay = homestayService.findById(homestayId);
        if (rooms.isEmpty()) return new HomestayCalendarResponse();
        List<Long> roomIds = rooms.stream().map(HomestayRoom::getId).toList();
        List<RoomCalendar> overrides = roomCalendarRepository.findCustomCalendarByRoomIdsAndDateRange(roomIds, startDate, endDate);
        List<BookingCalendarProjection> bookings = bookingDetailService.findActiveBookingsForCalendar(roomIds, startDate, endDate);        Map<Long, Map<LocalDate, RoomCalendar>> overrideMap = overrides.stream()
                .collect(Collectors.groupingBy(RoomCalendar::getRoomId,
                        Collectors.toMap(RoomCalendar::getNightDate, rc -> rc)));

        Map<Long, List<BookingCalendarProjection>> bookingMap = bookings.stream()
                .collect(Collectors.groupingBy(BookingCalendarProjection::getRoomId));
        List<RoomRatePlan> allRatePlans = roomRatePlanService.getAllRoomRatePlans(roomIds);
        Map<Long, List<RoomRatePlan>> ratePlanMap = allRatePlans.stream()
                .collect(Collectors.groupingBy(RoomRatePlan::getRoomId));

        List<CalendarRoomResponse> result = new ArrayList<>();
        for (HomestayRoom room : rooms) {
            List<CalendarInventoryResponse> inventoryList = new ArrayList<>();
            Map<LocalDate, RoomCalendar> roomOverrides = overrideMap.getOrDefault(room.getId(), Collections.emptyMap());
            List<BookingCalendarProjection> roomBookings = bookingMap.getOrDefault(room.getId(), Collections.emptyList());

            // XỬ LÝ LẤY GIÁ GỐC TỪ RATE PLAN (Lấy giá rẻ nhất làm giá đại diện hiển thị trên lịch)
            List<RoomRatePlan> roomPlans = ratePlanMap.getOrDefault(room.getId(), Collections.emptyList());
            BigDecimal basePrice = roomPlans.stream()
                    .map(RoomRatePlan::getPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            // Chạy từng ngày
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                inventoryList.add(buildDailyCell(date, room, roomOverrides.get(date), roomBookings,allRatePlans));
            }

            result.add(CalendarRoomResponse.builder()
                    .id(room.getId())
                    .name(room.getName())
                            .imageUrl(room.getImageUrl())
                    .tag(room.getTag())
                    .basePrice(basePrice)
                    .inventory(inventoryList)
                    .build());
        }

        return HomestayCalendarResponse.builder().owner(ownerResponse).homestayId(homestay.getId()).homestayName(homestay.getName()).status(homestay.getStatus()).roomCode("PRT").rooms(result).build();

    }

    @Override
    @Transactional
    public void batchUpdateCalendar(BatchUpdateCalendarRequest request) {
        Long roomId = request.getRoomId();
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        List<RoomCalendar> existingCalendars = roomCalendarRepository
                .findByRoomIdAndNightDateBetween(roomId, startDate, endDate);

        Map<LocalDate, RoomCalendar> calendarMap = existingCalendars.stream()
                .collect(Collectors.toMap(RoomCalendar::getNightDate, calendar -> calendar));

        List<RoomCalendar> calendarsToSave = new ArrayList<>();

        // 3. Duyệt qua dải ngày
        LocalDate current = startDate;
        while (!current.isAfter(endDate)) {
            RoomCalendar calendar = calendarMap.getOrDefault(current,
                    RoomCalendar.builder()
                            .roomId(roomId)
                            .nightDate(current)
                            .build());

            // Cập nhật thuộc tính
            boolean isChanged = false;

            if (request.getPriceOverride() != null && !request.getPriceOverride().equals(calendar.getPriceOverride())) {
                calendar.setPriceOverride(request.getPriceOverride());
                isChanged = true;
            }

            if (request.getAvailableQuantity() != null && !request.getAvailableQuantity().equals(calendar.getAvailableQuantity())) {
                calendar.setAvailableQuantity(request.getAvailableQuantity());
                isChanged = true;
            }

            if (RoomStatus.BLOCKED.equals(request.getStatus())) {
                if (calendar.getAvailableQuantity() != 0) {
                    calendar.setAvailableQuantity(0);
                    isChanged = true;
                }
            }

            if (isChanged || calendar.getId() == null) {
                calendarsToSave.add(calendar);
            }

            current = current.plusDays(1);
        }

        if (!calendarsToSave.isEmpty()) {
            roomCalendarRepository.saveAll(calendarsToSave);
        }
    }

    @Override
    public List<CalendarInventoryResponse> getCalendarDetails(Long roomId, LocalDate start, LocalDate end) {
        // 1. Lấy dữ liệu lịch và dữ liệu booking
        List<RoomCalendar> calendars = roomCalendarRepository.findByRoomIdAndNightDateBetween(roomId, start, end);
        List<BookingCalendarProjection> bookings = bookingDetailService.findActiveBookingsForCalendar(
                Collections.singletonList(roomId), start, end);

        // Lấy thông tin phòng để biết tổng số lượng (roomQuantity)
        HomestayRoom room = homestayRoomService.getRoomById(roomId);
        // 2. Chuyển calendars sang Map để dễ tra cứu
        Map<LocalDate, RoomCalendar> calMap = calendars.stream()
                .collect(Collectors.toMap(RoomCalendar::getNightDate, c -> c));

        List<CalendarInventoryResponse> result = new ArrayList<>();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            RoomCalendar cal = calMap.get(date);

            // --- SỬA Ở ĐÂY ---
            // Tạo biến cục bộ để sử dụng trong Lambda
            final LocalDate currentDate = date;

            // Tính booking trong ngày này
            int totalBookedInDay = (int) bookings.stream()
                    // Sử dụng currentDate thay vì date
                    .filter(b -> !currentDate.isBefore(b.getCheckInDate()) && currentDate.isBefore(b.getCheckOutDate()))
                    .mapToInt(BookingCalendarProjection::getQuantity)
                    .sum();

            // Gọi hàm determineStatus đã được sửa để nhận đủ tham số
            RoomCalendarStatus status = determineStatus(cal, totalBookedInDay, room.getQuantity());

            result.add(CalendarInventoryResponse.builder()
                    .date(currentDate) // Dùng currentDate ở đây
                    .priceOverride(cal != null ? cal.getPriceOverride() : null)
                    .availableQuantity(cal != null ? cal.getAvailableQuantity() : room.getQuantity())
                    .status(status)
                    .build());
        }
        return result;
    }

    private CalendarInventoryResponse buildDailyCell(LocalDate date, HomestayRoom room,
                                                     RoomCalendar cal,
                                                     List<BookingCalendarProjection> roomBookings,List<RoomRatePlan> roomRatePlans) {

        // 1. Tính toán booking trong ngày
        int totalBookedInDay = 0;
        List<BookingSimpleInfo> bookingInfos = new ArrayList<>();

        for (BookingCalendarProjection b : roomBookings) {
            if (!date.isBefore(b.getCheckInDate()) && date.isBefore(b.getCheckOutDate())) {
                totalBookedInDay += b.getQuantity();
                bookingInfos.add(new BookingSimpleInfo(b.getBookingCode(), b.getGuestName(), b.getQuantity()));
            }
        }

        // 2. Xác định Giá (Ưu tiên Price Override trước, sau đó mới đến giá gốc của phòng)
        BigDecimal finalPrice;
        if (cal != null && cal.getPriceOverride() != null) {
            finalPrice = cal.getPriceOverride();
        } else {
            finalPrice = roomRatePlans.stream()
                    .map(RoomRatePlan::getPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);
        }

        // 3. Xác định Số lượng còn lại (Available)
        // Nếu cal không tồn tại, mặc định là room.getQuantity()
        int availableQty = (cal != null && cal.getAvailableQuantity() != null)
                ? cal.getAvailableQuantity()
                : room.getQuantity();

        // 4. Xác định Trạng thái (Logic này cực kỳ quan trọng cho UI)
        RoomCalendarStatus status;

        // Kiểm tra ưu tiên: Đã có khách đặt chưa?
        if (totalBookedInDay >= room.getQuantity()) {
            status = RoomCalendarStatus.BOOKED; // Full phòng
        }
        // Nếu không full, kiểm tra Host có khóa phòng thủ công không?
        else if (availableQty <= 0) {
            status = RoomCalendarStatus.BLOCKED;
        }
        // Nếu vẫn còn phòng
        else {
            status = RoomCalendarStatus.AVAILABLE;
        }

        return CalendarInventoryResponse.builder()
                .date(date)
                .availableQuantity(availableQty)
                .status(status)
                .priceOverride(finalPrice) // Đã cập nhật giá đúng
                .totalBookedInDay(totalBookedInDay)
                .bookings(bookingInfos)
                .build();
    }
    private RoomCalendarStatus determineStatus(RoomCalendar cal, int totalBookedInDay, int roomQuantity) {
        // 1. ƯU TIÊN 1: Trạng thái Đã đặt (BOOKED)
        // Nếu số phòng đã bán >= tổng số phòng, thì dù Host có chỉnh available hay không,
        // trạng thái hiển thị trên lịch phải là BOOKED để tránh overbooking.
        if (totalBookedInDay >= roomQuantity) {
            return RoomCalendarStatus.BOOKED;
        }

        // 2. ƯU TIÊN 2: Trạng thái Khóa phòng (BLOCKED)
        // Nếu cal bị null (chưa cấu hình) hoặc Host chủ động set available = 0
        if (cal == null || cal.getAvailableQuantity() == null || cal.getAvailableQuantity() <= 0) {
            return RoomCalendarStatus.BLOCKED;
        }

        // 3. ƯU TIÊN 3: Trạng thái Sẵn sàng (AVAILABLE)
        // Còn phòng và không bị đặt full.
        return RoomCalendarStatus.AVAILABLE;
    }
}
