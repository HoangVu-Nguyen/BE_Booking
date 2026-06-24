package clyvasync.Clyvasync.service.room.impl;

import clyvasync.Clyvasync.dto.detail.BookingSimpleInfo;
import clyvasync.Clyvasync.dto.projection.BookingCalendarProjection;
import clyvasync.Clyvasync.dto.request.BatchUpdateCalendarRequest;
import clyvasync.Clyvasync.dto.response.*;
import clyvasync.Clyvasync.enums.calendar.RoomCalendarStatus;
import clyvasync.Clyvasync.enums.homestay.HomestayStatus;
import clyvasync.Clyvasync.enums.room.RoomStatus;
import clyvasync.Clyvasync.modules.booking.entity.BookingDetail;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayRoom;
import clyvasync.Clyvasync.modules.room.*;
import clyvasync.Clyvasync.repository.room.*;
import clyvasync.Clyvasync.service.auth.UserService;
import clyvasync.Clyvasync.service.booking.BookingDetailService;
import clyvasync.Clyvasync.service.homestay.HomestayRoomService;
import clyvasync.Clyvasync.service.homestay.HomestayService;
import clyvasync.Clyvasync.service.room.RoomCalendarService;
import clyvasync.Clyvasync.service.room.RoomRatePlanService;
import clyvasync.Clyvasync.utils.MediaUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
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
    private final RoomBedRepository roomBedRepository;
    private final RoomImageRepository roomImageRepository;
    private final RoomRatePlanRepository roomRatePlanRepository;
    private final RatePlanCalendarRepository ratePlanCalendarRepository;
    private final MediaUtil mediaUtil;

    public RoomCalendarServiceImpl(RoomCalendarRepository roomCalendarRepository, HomestayRoomService homestayRoomService, BookingDetailService bookingDetailService, RoomRatePlanService roomRatePlanService, @Lazy HomestayService homestayService, UserService userService, RoomBedRepository roomBedRepository, RoomImageRepository roomImageRepository, RoomRatePlanRepository roomRatePlanRepository, RatePlanCalendarRepository ratePlanCalendarRepository,MediaUtil mediaUtil) {
        this.roomCalendarRepository = roomCalendarRepository;
        this.homestayRoomService = homestayRoomService;
        this.bookingDetailService = bookingDetailService;
        this.roomRatePlanService = roomRatePlanService;
        this.homestayService = homestayService;
        this.userService = userService;
        this.roomBedRepository = roomBedRepository;
        this.roomImageRepository = roomImageRepository;
        this.roomRatePlanRepository = roomRatePlanRepository;
        this.ratePlanCalendarRepository = ratePlanCalendarRepository;
        this.mediaUtil = mediaUtil;
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
    public HomestayCalendarResponse getHomestayCalendar(Long ownerId, Long homestayId, LocalDate startDate, LocalDate endDate) {

        OwnerResponse ownerResponse = userService.getOwnerInfo(ownerId);
        Homestay homestay = homestayService.findById(homestayId);
        List<HomestayRoom> rooms = homestayRoomService.findAllByHomestayIdAndStatus(homestayId, RoomStatus.ACTIVE);

        if (rooms.isEmpty()) {
            return new HomestayCalendarResponse();
        }

        List<Long> roomIds = rooms.stream().map(HomestayRoom::getId).toList();

        // 1. KÉO DỮ LIỆU TỒN PHÒNG & ĐẶT PHÒNG
        List<RoomCalendar> overrides = roomCalendarRepository.findCustomCalendarByRoomIdsAndDateRange(roomIds, startDate, endDate);
        List<BookingCalendarProjection> bookings = bookingDetailService.findActiveBookingsForCalendar(roomIds, startDate, endDate);

        // 2. KÉO DỮ LIỆU GÓI GIÁ & LỊCH GIÁ (MỚI)
        List<RoomRatePlan> allRatePlans = roomRatePlanService.getAllRoomRatePlans(roomIds);
        List<Long> ratePlanIds = allRatePlans.stream().map(RoomRatePlan::getId).toList();
        // 👉 Kéo giá biến động của tất cả các gói giá trong homestay này
        List<RatePlanCalendar> rateCalendars = ratePlanCalendarRepository.findByRatePlanIdInAndNightDateBetween(ratePlanIds, startDate, endDate);

        // 3. KÉO DỮ LIỆU ẢNH & GIƯỜNG
        List<RoomImage> allImages = roomImageRepository.findAllByRoomIdIn(roomIds);
        List<RoomBed> allBeds = roomBedRepository.findByRoomIdIn(roomIds);

        // ==========================================
        // MEMORY GROUPING (GOM NHÓM VÀO MAP TRÊN RAM)
        // ==========================================
        Map<Long, Map<LocalDate, RoomCalendar>> overrideMap = overrides.stream()
                .collect(Collectors.groupingBy(RoomCalendar::getRoomId,
                        Collectors.toMap(RoomCalendar::getNightDate, rc -> rc)));

        Map<Long, List<BookingCalendarProjection>> bookingMap = bookings.stream()
                .collect(Collectors.groupingBy(BookingCalendarProjection::getRoomId));

        Map<Long, List<RoomRatePlan>> ratePlanMap = allRatePlans.stream()
                .collect(Collectors.groupingBy(RoomRatePlan::getRoomId));

        // 👉 Map Lịch Giá theo chuẩn: RatePlanId -> (NightDate -> RatePlanCalendar)
        Map<Long, Map<LocalDate, RatePlanCalendar>> rateCalendarMap = rateCalendars.stream()
                .collect(Collectors.groupingBy(RatePlanCalendar::getRatePlanId,
                        Collectors.toMap(RatePlanCalendar::getNightDate, rpc -> rpc)));

        Map<Long, List<RoomImage>> imagesMap = allImages.stream()
                .collect(Collectors.groupingBy(RoomImage::getRoomId));

        Map<Long, List<RoomBed>> bedsMap = allBeds.stream()
                .collect(Collectors.groupingBy(RoomBed::getRoomId));

        List<CalendarRoomResponse> result = new ArrayList<>();

        // ==========================================
        // LẮP RÁP KẾT QUẢ CHO TỪNG PHÒNG
        // ==========================================
        for (HomestayRoom room : rooms) {
            List<CalendarInventoryResponse> inventoryList = new ArrayList<>();
            Map<LocalDate, RoomCalendar> roomOverrides = overrideMap.getOrDefault(room.getId(), Collections.emptyMap());
            List<BookingCalendarProjection> roomBookings = bookingMap.getOrDefault(room.getId(), Collections.emptyList());

            // Lấy danh sách gói giá CỦA RIÊNG PHÒNG NÀY
            List<RoomRatePlan> roomPlans = ratePlanMap.getOrDefault(room.getId(), Collections.emptyList());

            BigDecimal basePrice = roomPlans.stream()
                    .map(RoomRatePlan::getPrice)
                    .min(BigDecimal::compareTo)
                    .orElse(BigDecimal.ZERO);

            List<RoomImage> roomImages = imagesMap.getOrDefault(room.getId(), Collections.emptyList());
            List<RoomImageResponse> imageResponses = roomImages.stream()
                    .map(img -> new RoomImageResponse(img.getId(), this.mediaUtil.toCdnUrl(img.getImageUrl()) , img.getIsCover()))
                    .toList();

            List<RoomBed> roomBeds = bedsMap.getOrDefault(room.getId(), Collections.emptyList());
            List<BedResponse> bedResponses = roomBeds.stream()
                    .map(b -> new BedResponse(b.getId(), b.getBedType(), b.getQuantity()))
                    .toList();

            // CHẠY VÒNG LẶP CHO TỪNG NGÀY ĐỂ BUILD LỊCH
            for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
                final LocalDate currentDate = date;

                // 👉 Lấy riêng bảng giá của các gói thuộc phòng này trong ngày hiện tại
                Map<Long, RatePlanCalendar> dailyPrices = new HashMap<>();
                for (RoomRatePlan rp : roomPlans) {
                    Map<LocalDate, RatePlanCalendar> rpCalMap = rateCalendarMap.getOrDefault(rp.getId(), Collections.emptyMap());
                    if (rpCalMap.containsKey(currentDate)) {
                        dailyPrices.put(rp.getId(), rpCalMap.get(currentDate));
                    }
                }

                // Gọi hàm buildDailyCell mới (Đã truyền roomPlans thay vì allRatePlans, và thêm dailyPrices)
                inventoryList.add(buildDailyCell(currentDate, room, roomOverrides.get(currentDate), roomBookings, roomPlans, dailyPrices));
            }

            result.add(CalendarRoomResponse.builder()
                    .id(room.getId())
                    .name(room.getName())
                    .beds(bedResponses)
                    .images(imageResponses)
                    .tag(room.getTag())
                    .basePrice(basePrice)
                    .inventory(inventoryList)
                    .build());
        }

        return HomestayCalendarResponse.builder()
                .owner(ownerResponse)
                .homestayId(homestay.getId())
                .homestayName(homestay.getName())
                .status(homestay.getStatus())
                .roomCode("PRT")
                .rooms(result)
                .build();
    }

    @Override
    @Transactional
    public void batchUpdateCalendar(BatchUpdateCalendarRequest request) {
        Long roomId = request.getRoomId();
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        if (roomId == null) {
            throw new IllegalArgumentException("roomId không được null");
        }

        if (startDate == null || endDate == null) {
            throw new IllegalArgumentException("startDate và endDate không được null");
        }

        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("endDate không được nhỏ hơn startDate");
        }

        String actionType = request.getActionType();

        if ("price".equalsIgnoreCase(actionType)) {
            updateRatePlanPrices(request);
            return;
        }

        if ("inventory".equalsIgnoreCase(actionType)) {
            updateRoomInventory(request);
            return;
        }

        if ("status".equalsIgnoreCase(actionType)) {
            updateRoomStatus(request);
            return;
        }

        // fallback: nếu FE không gửi actionType thì update tất cả cái nào có data
        updateRoomInventoryAndStatusIfPresent(request);
        updateRatePlanPrices(request);
    }
    private void updateRoomInventory(BatchUpdateCalendarRequest request) {
        Long roomId = request.getRoomId();
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        if (request.getAvailableQuantity() == null) {
            throw new IllegalArgumentException("availableQuantity không được null khi cập nhật tồn kho");
        }

        List<RoomCalendar> existingCalendars =
                roomCalendarRepository.findByRoomIdAndNightDateBetween(roomId, startDate, endDate);

        Map<LocalDate, RoomCalendar> calendarMap = existingCalendars.stream()
                .collect(Collectors.toMap(
                        RoomCalendar::getNightDate,
                        calendar -> calendar,
                        (oldValue, newValue) -> newValue
                ));

        List<RoomCalendar> calendarsToSave = new ArrayList<>();

        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            RoomCalendar calendar = calendarMap.getOrDefault(
                    current,
                    RoomCalendar.builder()
                            .roomId(roomId)
                            .nightDate(current)
                            .build()
            );

            boolean changed = false;

            if (!Objects.equals(calendar.getAvailableQuantity(), request.getAvailableQuantity())) {
                calendar.setAvailableQuantity(request.getAvailableQuantity());
                changed = true;
            }

            if (calendar.getStatus() == null) {
                calendar.setStatus(RoomCalendarStatus.AVAILABLE);
                changed = true;
            }

            if (changed || calendar.getId() == null) {
                calendarsToSave.add(calendar);
            }

            current = current.plusDays(1);
        }

        if (!calendarsToSave.isEmpty()) {
            roomCalendarRepository.saveAll(calendarsToSave);
        }
    }
    private void updateRoomStatus(BatchUpdateCalendarRequest request) {
        Long roomId = request.getRoomId();
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        if (request.getStatus() == null) {
            throw new IllegalArgumentException("status không được null khi cập nhật trạng thái");
        }

        List<RoomCalendar> existingCalendars =
                roomCalendarRepository.findByRoomIdAndNightDateBetween(roomId, startDate, endDate);

        Map<LocalDate, RoomCalendar> calendarMap = existingCalendars.stream()
                .collect(Collectors.toMap(
                        RoomCalendar::getNightDate,
                        calendar -> calendar,
                        (oldValue, newValue) -> newValue
                ));

        List<RoomCalendar> calendarsToSave = new ArrayList<>();

        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            RoomCalendar calendar = calendarMap.getOrDefault(
                    current,
                    RoomCalendar.builder()
                            .roomId(roomId)
                            .nightDate(current)
                            .build()
            );

            boolean changed = false;

            if (!Objects.equals(calendar.getStatus(), request.getStatus())) {
                calendar.setStatus(request.getStatus());
                changed = true;
            }

            if (request.getStatus() == RoomCalendarStatus.BLOCKED
                    || request.getStatus() == RoomCalendarStatus.MAINTENANCE) {
                if (!Objects.equals(calendar.getAvailableQuantity(), 0)) {
                    calendar.setAvailableQuantity(0);
                    changed = true;
                }
            }

            if (request.getStatus() == RoomCalendarStatus.AVAILABLE
                    && request.getAvailableQuantity() != null) {
                if (!Objects.equals(calendar.getAvailableQuantity(), request.getAvailableQuantity())) {
                    calendar.setAvailableQuantity(request.getAvailableQuantity());
                    changed = true;
                }
            }

            if (changed || calendar.getId() == null) {
                calendarsToSave.add(calendar);
            }

            current = current.plusDays(1);
        }

        if (!calendarsToSave.isEmpty()) {
            roomCalendarRepository.saveAll(calendarsToSave);
        }
    }
    private void updateRoomInventoryAndStatusIfPresent(BatchUpdateCalendarRequest request) {
        Long roomId = request.getRoomId();
        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        if (request.getAvailableQuantity() == null && request.getStatus() == null) {
            return;
        }

        List<RoomCalendar> existingCalendars =
                roomCalendarRepository.findByRoomIdAndNightDateBetween(roomId, startDate, endDate);

        Map<LocalDate, RoomCalendar> calendarMap = existingCalendars.stream()
                .collect(Collectors.toMap(
                        RoomCalendar::getNightDate,
                        calendar -> calendar,
                        (oldValue, newValue) -> newValue
                ));

        List<RoomCalendar> calendarsToSave = new ArrayList<>();

        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            RoomCalendar calendar = calendarMap.getOrDefault(
                    current,
                    RoomCalendar.builder()
                            .roomId(roomId)
                            .nightDate(current)
                            .build()
            );

            boolean changed = false;

            if (request.getAvailableQuantity() != null
                    && !Objects.equals(calendar.getAvailableQuantity(), request.getAvailableQuantity())) {
                calendar.setAvailableQuantity(request.getAvailableQuantity());
                changed = true;
            }

            if (request.getStatus() != null
                    && !Objects.equals(calendar.getStatus(), request.getStatus())) {
                calendar.setStatus(request.getStatus());
                changed = true;
            }

            if ((request.getStatus() == RoomCalendarStatus.BLOCKED
                    || request.getStatus() == RoomCalendarStatus.MAINTENANCE)
                    && !Objects.equals(calendar.getAvailableQuantity(), 0)) {
                calendar.setAvailableQuantity(0);
                changed = true;
            }

            if (changed || calendar.getId() == null) {
                calendarsToSave.add(calendar);
            }

            current = current.plusDays(1);
        }

        if (!calendarsToSave.isEmpty()) {
            roomCalendarRepository.saveAll(calendarsToSave);
        }
    }
    private void updateRatePlanPrices(BatchUpdateCalendarRequest request) {
        List<BatchUpdateCalendarRequest.RatePlanUpdateRequest> ratePlanUpdates =
                request.getRatePlanUpdates();

        if (ratePlanUpdates == null || ratePlanUpdates.isEmpty()) {
            return;
        }

        LocalDate startDate = request.getStartDate();
        LocalDate endDate = request.getEndDate();

        List<Long> ratePlanIds = ratePlanUpdates.stream()
                .map(BatchUpdateCalendarRequest.RatePlanUpdateRequest::getRatePlanId)
                .filter(Objects::nonNull)
                .distinct()
                .toList();

        if (ratePlanIds.isEmpty()) {
            return;
        }

        List<RatePlanCalendar> existingRateCalendars =
                ratePlanCalendarRepository.findByRatePlanIdInAndNightDateBetween(
                        ratePlanIds,
                        startDate,
                        endDate
                );

        Map<String, RatePlanCalendar> rateCalendarMap = existingRateCalendars.stream()
                .collect(Collectors.toMap(
                        calendar -> buildRatePlanCalendarKey(calendar.getRatePlanId(), calendar.getNightDate()),
                        calendar -> calendar,
                        (oldValue, newValue) -> newValue
                ));

        List<RatePlanCalendar> calendarsToSave = new ArrayList<>();

        LocalDate current = startDate;

        while (!current.isAfter(endDate)) {
            for (BatchUpdateCalendarRequest.RatePlanUpdateRequest update : ratePlanUpdates) {
                if (update.getRatePlanId() == null || update.getPrice() == null) {
                    continue;
                }

                if (update.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
                    throw new IllegalArgumentException("Giá rate plan phải lớn hơn 0");
                }

                String key = buildRatePlanCalendarKey(update.getRatePlanId(), current);

                RatePlanCalendar calendar = rateCalendarMap.getOrDefault(
                        key,
                        RatePlanCalendar.builder()
                                .ratePlanId(update.getRatePlanId())
                                .nightDate(current)
                                .build()
                );

                if (!Objects.equals(calendar.getPrice(), update.getPrice())) {
                    calendar.setPrice(update.getPrice());
                    calendarsToSave.add(calendar);
                } else if (calendar.getId() == null) {
                    calendarsToSave.add(calendar);
                }
            }

            current = current.plusDays(1);
        }

        if (!calendarsToSave.isEmpty()) {
            ratePlanCalendarRepository.saveAll(calendarsToSave);
        }
    }
    private String buildRatePlanCalendarKey(Long ratePlanId, LocalDate nightDate) {
        return ratePlanId + "_" + nightDate;
    }
    @Override
    public List<CalendarInventoryResponse> getCalendarDetails(
            Long roomId,
            LocalDate start,
            LocalDate end
    ) {
        HomestayRoom room = homestayRoomService.getRoomById(roomId);

        List<RoomCalendar> inventoryCalendars =
                roomCalendarRepository.findByRoomIdAndNightDateBetween(roomId, start, end);

        List<BookingCalendarProjection> bookings =
                bookingDetailService.findActiveBookingsForCalendar(
                        Collections.singletonList(roomId),
                        start,
                        end
                );

        List<RoomRatePlan> ratePlans =
                roomRatePlanRepository.findAllRoomRatePlanByRoomId(roomId);

        List<Long> ratePlanIds = ratePlans.stream()
                .map(RoomRatePlan::getId)
                .toList();

        List<RatePlanCalendar> rateCalendars = ratePlanIds.isEmpty()
                ? List.of()
                : ratePlanCalendarRepository.findByRatePlanIdInAndNightDateBetween(
                ratePlanIds,
                start,
                end
        );

        Map<LocalDate, RoomCalendar> inventoryMap = inventoryCalendars.stream()
                .collect(Collectors.toMap(
                        RoomCalendar::getNightDate,
                        calendar -> calendar,
                        (existing, replacement) -> replacement
                ));

        Map<LocalDate, Map<Long, RatePlanCalendar>> priceMap = rateCalendars.stream()
                .collect(Collectors.groupingBy(
                        RatePlanCalendar::getNightDate,
                        Collectors.toMap(
                                RatePlanCalendar::getRatePlanId,
                                calendar -> calendar,
                                (existing, replacement) -> replacement
                        )
                ));

        List<CalendarInventoryResponse> result = new ArrayList<>();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            LocalDate currentDate = date;

            RoomCalendar inventoryCalendar = inventoryMap.get(currentDate);

            int totalBookedInDay = bookings.stream()
                    .filter(booking ->
                            !currentDate.isBefore(booking.getCheckInDate())
                                    && currentDate.isBefore(booking.getCheckOutDate())
                    )
                    .mapToInt(BookingCalendarProjection::getQuantity)
                    .sum();

            List<BookingSimpleInfo> bookingInfos = bookings.stream()
                    .filter(booking ->
                            !currentDate.isBefore(booking.getCheckInDate())
                                    && currentDate.isBefore(booking.getCheckOutDate())
                    )
                    .map(booking -> new BookingSimpleInfo(
                            booking.getBookingCode(),
                            booking.getGuestName(),
                            booking.getQuantity()
                    ))
                    .toList();

            Map<Long, RatePlanCalendar> dailyPrices =
                    priceMap.getOrDefault(currentDate, Collections.emptyMap());

            List<RatePlanPriceResponse> ratePlanPrices = ratePlans.stream()
                    .map(ratePlan -> {
                        RatePlanCalendar dailyPrice = dailyPrices.get(ratePlan.getId());
                        boolean hasOverride = dailyPrice != null;

                        return RatePlanPriceResponse.builder()
                                .ratePlanId(ratePlan.getId())
                                .name(ratePlan.getName())
                                .basePrice(ratePlan.getPrice())
                                .price(hasOverride ? dailyPrice.getPrice() : ratePlan.getPrice())
                                .hasOverride(hasOverride)
                                .build();
                    })
                    .toList();

            BigDecimal displayPrice = ratePlanPrices.stream()
                    .map(RatePlanPriceResponse::getPrice)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(null);

            BigDecimal minOverridePrice = ratePlanPrices.stream()
                    .filter(ratePlanPrice -> Boolean.TRUE.equals(ratePlanPrice.getHasOverride()))
                    .map(RatePlanPriceResponse::getPrice)
                    .filter(Objects::nonNull)
                    .min(BigDecimal::compareTo)
                    .orElse(null);

            boolean hasPriceOverride = minOverridePrice != null;

            int availableQuantity = inventoryCalendar != null
                    ? inventoryCalendar.getAvailableQuantity()
                    : room.getQuantity();

            RoomCalendarStatus status = determineStatus(
                    inventoryCalendar,
                    totalBookedInDay,
                    room.getQuantity()
            );

            result.add(CalendarInventoryResponse.builder()
                    .date(currentDate)
                    .displayPrice(displayPrice)
                    .priceOverride(minOverridePrice)
                    .hasPriceOverride(hasPriceOverride)
                    .availableQuantity(availableQuantity)
                    .status(status)
                    .totalBookedInDay(totalBookedInDay)
                    .bookings(bookingInfos)
                    .ratePlanPrices(ratePlanPrices)
                    .build());
        }

        return result;
    }

    private CalendarInventoryResponse buildDailyCell(
            LocalDate date,
            HomestayRoom room,
            RoomCalendar cal,
            List<BookingCalendarProjection> roomBookings,
            List<RoomRatePlan> roomRatePlans,
            Map<Long, RatePlanCalendar> dailyPrices // 👉 BỔ SUNG THAM SỐ NÀY
    ) {

        // 1. Tính toán booking trong ngày
        int totalBookedInDay = 0;
        List<BookingSimpleInfo> bookingInfos = new ArrayList<>();

        for (BookingCalendarProjection b : roomBookings) {
            if (!date.isBefore(b.getCheckInDate()) && date.isBefore(b.getCheckOutDate())) {
                totalBookedInDay += b.getQuantity();
                bookingInfos.add(new BookingSimpleInfo(b.getBookingCode(), b.getGuestName(), b.getQuantity()));
            }
        }

        // 2. Xác định Giá Rẻ Nhất (👉 ĐÃ SỬA: Quét qua tất cả gói giá, ưu tiên giá của ngày đó)
        BigDecimal finalPrice = roomRatePlans.stream()
                .map(rp -> {
                    // Nếu ngày này Host có cài giá riêng cho gói đó -> Lấy giá riêng
                    if (dailyPrices != null && dailyPrices.containsKey(rp.getId())) {
                        return dailyPrices.get(rp.getId()).getPrice();
                    }
                    // Nếu không có đè giá -> Lấy giá gốc của gói
                    return rp.getPrice();
                })
                .min(BigDecimal::compareTo)
                .orElse(BigDecimal.ZERO);

        // 3. Xác định Số lượng còn lại (Available)
        // Nếu cal không tồn tại, mặc định là tổng số lượng phòng ban đầu
        int availableQty = (cal != null && cal.getAvailableQuantity() != null)
                ? cal.getAvailableQuantity()
                : room.getQuantity();

        // 4. Xác định Trạng thái (Logic UI)
        RoomCalendarStatus status;

        // Kiểm tra ưu tiên: Đã có khách đặt full chưa?
        if (totalBookedInDay >= room.getQuantity()) {
            status = RoomCalendarStatus.BOOKED;
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
                .priceOverride(finalPrice) // Giá rẻ nhất đã được tính chuẩn xác
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
    private void syncSearchIndexAfterInventoryUpdate(Long roomId) {
        HomestayRoom room = homestayRoomService.getRoomById(roomId);

    }
}
