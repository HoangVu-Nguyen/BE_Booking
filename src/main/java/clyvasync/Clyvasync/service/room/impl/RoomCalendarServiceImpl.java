package clyvasync.Clyvasync.service.room.impl;

import clyvasync.Clyvasync.dto.detail.BookingSimpleInfo;
import clyvasync.Clyvasync.dto.projection.BookingCalendarProjection;
import clyvasync.Clyvasync.dto.response.CalendarInventoryResponse;
import clyvasync.Clyvasync.dto.response.CalendarRoomResponse;
import clyvasync.Clyvasync.enums.calendar.RoomCalendarStatus;
import clyvasync.Clyvasync.enums.homestay.HomestayStatus;
import clyvasync.Clyvasync.enums.room.RoomStatus;
import clyvasync.Clyvasync.modules.booking.entity.BookingDetail;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayRoom;
import clyvasync.Clyvasync.modules.room.RoomCalendar;
import clyvasync.Clyvasync.modules.room.RoomRatePlan;
import clyvasync.Clyvasync.repository.room.RoomCalendarRepository;
import clyvasync.Clyvasync.service.booking.BookingDetailService;
import clyvasync.Clyvasync.service.homestay.HomestayRoomService;
import clyvasync.Clyvasync.service.room.RoomCalendarService;
import clyvasync.Clyvasync.service.room.RoomRatePlanService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class RoomCalendarServiceImpl implements RoomCalendarService {
    private final RoomCalendarRepository roomCalendarRepository;
    private final HomestayRoomService homestayRoomService;
    private final BookingDetailService bookingDetailService;
    private final RoomRatePlanService roomRatePlanService;

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
    public List<CalendarRoomResponse> getHomestayCalendar(Long homestayId, LocalDate startDate, LocalDate endDate) {
        List<HomestayRoom> rooms = homestayRoomService.findAllByHomestayIdAndStatus(homestayId, RoomStatus.ACTIVE);
        if (rooms.isEmpty()) return Collections.emptyList();
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
                inventoryList.add(buildDailyCell(date, room, roomOverrides.get(date), roomBookings));
            }

            result.add(CalendarRoomResponse.builder()
                    .id(room.getId())
                    .name(room.getName())
                    .tag(room.getTag())
                    .basePrice(basePrice)
                    .inventory(inventoryList)
                    .build());
        }

        return result;

    }
    private CalendarInventoryResponse buildDailyCell(LocalDate date, HomestayRoom room,
                                                     RoomCalendar cal, // Dòng dữ liệu từ bảng RoomCalendar trong ảnh
                                                     List<BookingCalendarProjection> roomBookings) {

        // 1. Số lượng phòng ban đầu (Ví dụ 8 như trong ảnh của bạn)
        int initialQty = room.getQuantity();

        // 2. Tính xem trong ngày đó có bao nhiêu phòng đã được đặt
        int totalBookedInDay = 0;
        List<BookingSimpleInfo> bookingInfos = new ArrayList<>();
        BookingCalendarProjection lastBooking = null;

        for (BookingCalendarProjection b : roomBookings) {
            if (!date.isBefore(b.getCheckInDate()) && date.isBefore(b.getCheckOutDate())) {
                totalBookedInDay += b.getQuantity();
                bookingInfos.add(new BookingSimpleInfo(b.getBookingCode(), b.getGuestName(), b.getQuantity()));
                lastBooking = b;
            }
        }

        // 3. Logic xác định trạng thái (KHÔNG CẦN OVERRIDE)
        RoomCalendarStatus status;

        // NẾU cal.getAvailableQuantity() == 0, ta cần biết tại sao?
        // Nếu totalBookedInDay > 0 -> Có nghĩa là khách đặt hết sạch phòng -> BOOKED
        // Nếu totalBookedInDay == 0 -> Nghĩa là không có khách nào đặt mà phòng vẫn 0 -> BLOCKED (Host khóa thật sự)

        if (cal.getAvailableQuantity() <= 0) {
            if (totalBookedInDay > 0) {
                status = RoomCalendarStatus.BOOKED; // Khách đặt full
            } else {
                status = RoomCalendarStatus.BLOCKED; // Host khóa phòng
            }
        } else {
            status = RoomCalendarStatus.AVAILABLE;
        }

        return CalendarInventoryResponse.builder()
                .date(date)
                .availableQuantity(cal.getAvailableQuantity())
                .status(status)
                .totalBookedInDay(totalBookedInDay)
                .bookings(bookingInfos)
                .bookingCode(lastBooking != null ? lastBooking.getBookingCode() : null)
                .build();
    }
}
