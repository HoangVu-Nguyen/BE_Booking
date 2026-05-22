package clyvasync.Clyvasync.service.room.impl;

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
                                                     RoomCalendar override,
                                                     List<BookingCalendarProjection> roomBookings) {

        // 1. Khởi tạo: Lấy tổng số lượng phòng từ loại phòng (HomestayRoom)
        Integer currentQty = room.getQuantity();
        RoomCalendarStatus status = RoomCalendarStatus.AVAILABLE;
        java.math.BigDecimal finalPrice = null;
        String bookingCode = null;
        String guestName = null;

        // 2. ƯU TIÊN 1: Ghi đè (Override) - Có thể làm thay đổi số lượng phòng tối đa hoặc giá
        if (override != null) {
            finalPrice = override.getPriceOverride();
            if (override.getAvailableQuantity() != null) {
                currentQty = override.getAvailableQuantity();
            }
            // Nếu Host chủ động set số phòng về 0 thì Block luôn
            if (currentQty <= 0) {
                status = RoomCalendarStatus.BLOCKED;
            }
        }

        // 3. ƯU TIÊN 2: Tính toán Inventory (Cộng dồn tất cả các booking trong ngày)
        // Chúng ta KHÔNG dùng break ở đây nữa vì cần tính tổng số phòng đã đặt trong ngày
        if (status != RoomCalendarStatus.BLOCKED) {
            int totalBookedInDay = 0;
            BookingCalendarProjection lastBooking = null;

            for (BookingCalendarProjection b : roomBookings) {
                // Kiểm tra xem ngày này có thuộc khoảng đêm lưu trú không
                if (!date.isBefore(b.getCheckInDate()) && date.isBefore(b.getCheckOutDate())) {
                    totalBookedInDay += b.getQuantity();
                    lastBooking = b; // Lưu lại để lấy info hiển thị
                }
            }

            // Trừ số lượng phòng đã bán
            currentQty -= totalBookedInDay;

            // Nếu hết phòng thì chuyển sang trạng thái BOOKED
            if (currentQty <= 0) {
                status = RoomCalendarStatus.BOOKED;
                // Hiển thị info của booking gần nhất hoặc booking cuối cùng
                if (lastBooking != null) {
                    bookingCode = lastBooking.getBookingCode();
                    guestName = (lastBooking.getGuestName() != null && !lastBooking.getGuestName().isBlank())
                            ? lastBooking.getGuestName()
                            : "KH: " + lastBooking.getBookingCode();
                }
            }
        }

        return CalendarInventoryResponse.builder()
                .date(date)
                .priceOverride(finalPrice)
                .availableQuantity(Math.max(0, currentQty)) // Hiển thị số phòng trống thực tế
                .status(status)
                .bookingCode(bookingCode)
                .guestName(guestName)
                .build();
    }
}
