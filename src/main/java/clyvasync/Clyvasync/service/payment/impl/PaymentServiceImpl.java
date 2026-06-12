package clyvasync.Clyvasync.service.payment.impl;

import clyvasync.Clyvasync.dto.event.BookingPaidEvent;
import clyvasync.Clyvasync.dto.response.PaymentConfirmResponse;
import clyvasync.Clyvasync.dto.response.UserPaymentMethodResponse;
import clyvasync.Clyvasync.enums.booking.BookingStatus;
import clyvasync.Clyvasync.enums.type.PaymentStatus;
import clyvasync.Clyvasync.enums.type.PayoutStatus;
import clyvasync.Clyvasync.enums.type.TourBookingStatus;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.factory.PaymentFactory;
import clyvasync.Clyvasync.modules.booking.entity.Booking;
import clyvasync.Clyvasync.modules.booking.entity.BookingDetail;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayRoom;
import clyvasync.Clyvasync.modules.payment.entity.UserPaymentMethod;
import clyvasync.Clyvasync.modules.tour.entity.TourBooking;
import clyvasync.Clyvasync.repository.booking.BookingRepository;
import clyvasync.Clyvasync.repository.payment.UserPaymentMethodRepository;
import clyvasync.Clyvasync.service.booking.BookingDetailService;
import clyvasync.Clyvasync.service.booking.BookingService;
import clyvasync.Clyvasync.service.homestay.HomestayRoomService;
import clyvasync.Clyvasync.service.homestay.HomestayService;
import clyvasync.Clyvasync.service.notification.NotificationService;
import clyvasync.Clyvasync.service.payment.PaymentService;
import clyvasync.Clyvasync.service.tour.TourBookingService;
import clyvasync.Clyvasync.service.wallet.HostWalletService;
import clyvasync.Clyvasync.strategy.PaymentStrategy;
import com.stripe.Stripe;
import com.stripe.model.PaymentMethod;
import com.stripe.model.SetupIntent;
import com.stripe.param.SetupIntentCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl  implements PaymentService {

    private final PaymentFactory paymentFactory;
    private final BookingService bookingService;
    private final BookingRepository bookingRepository;

    // Inject thêm các Service bác đã viết để cào dữ liệu chuẩn xác
    private final BookingDetailService bookingDetailService;
    private final HomestayRoomService homestayRoomService;
    private final TourBookingService tourBookingService;
    private final HostWalletService hostWalletService;
    private final HomestayService homestayService;
    private final ApplicationEventPublisher eventPublisher; // Kênh phát thanh sự kiện
    @Value("${stripe.api.key}")
    private String stripeApiKey;
    private final UserPaymentMethodRepository paymentMethodRepository;
    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey; // Khởi tạo Secret Key của Stripe
    }
    /**
     * Logic sinh link URL thanh toán
     */
    public String createUrl(String bookingCode, clyvasync.Clyvasync.enums.payment.PaymentMethod method, HttpServletRequest request) {
        Booking booking = bookingService.getBookingByCode(bookingCode);
        PaymentStrategy strategy = paymentFactory.getStrategy(method);
        return strategy.createPaymentUrl(booking, request);
    }

    /**
     * Logic nghiệp vụ xử lý dữ liệu trả về cho Angular render UI thành công
     * FIX: Sử dụng đúng Service để lấy Room và Tours, không dùng hàm get quan hệ bị khuyết
     */
    public Map<String, Object> processPaymentReturn(String gateway, Map<String, String> params) {
        clyvasync.Clyvasync.enums.payment.PaymentMethod method = clyvasync.Clyvasync.enums.payment.PaymentMethod.valueOf(gateway.toUpperCase());
        PaymentStrategy strategy = paymentFactory.getStrategy(method);

        // 1. Xác thực chữ ký số từ đối tác tránh đổi tham số URL ở FE
        PaymentStatus status = strategy.processCallback(params);
        String bookingCode = strategy.extractBookingCode(params);

        Booking booking = bookingService.getBookingByCode(bookingCode);

        if (status == PaymentStatus.PAID) {
            Map<String, Object> data = new HashMap<>();
            data.put("bookingCode", booking.getBookingCode());
            data.put("totalPrice", booking.getTotalPrice());
            data.put("guestName", booking.getGuestName());

            // 2. Lấy thông tin phòng chuẩn chỉ bằng bookingDetailService bác cung cấp
            BookingDetail detail = bookingDetailService.findBookingDetailByBookingId(booking.getId());
            if (detail != null) {
                HomestayRoom room = homestayRoomService.getRoomById(detail.getRoomId());
                data.put("roomName", room != null ? room.getName() : "N/A");
                data.put("guests", detail.getGuestCount() + " Guests");
            }

            // 3. Lấy danh sách tour đính kèm từ đúng tourBookingService của hệ thống
            List<TourBooking> tourBookings = tourBookingService.findAllByHomestayBookingId(booking.getId());
            data.put("tours", tourBookings);

            return data;
        }

        throw new AppException(ResultCode.PAYMENT_FAILED_OR_CANCELLED);
    }

    /**
     * Logic nghiệp vụ IPN Webhook chạy ngầm - Chốt đơn lưu trữ xuống DB thực tế
     * FIX: Đồng bộ cập nhật luôn trạng thái của tour_bookings đi kèm cho chuẩn luồng dữ liệu
     */
    @Transactional
    public ResponseEntity<?> processPaymentIPN(String gateway, Map<String, String> params) {
        clyvasync.Clyvasync.enums.payment.PaymentMethod method = clyvasync.Clyvasync.enums.payment.PaymentMethod.valueOf(gateway.toUpperCase());
        PaymentStrategy strategy = paymentFactory.getStrategy(method);

        PaymentStatus paymentStatus = strategy.processCallback(params);
        String bookingCode = strategy.extractBookingCode(params);
        Booking booking = bookingService.getBookingByCode(bookingCode);

        // Idempotency check
        if (PaymentStatus.PAID.equals(booking.getPaymentStatus())) {
            return strategy.buildIPNSuccessResponse("Đơn hàng này đã được xác nhận thành công từ trước");
        }

        if (paymentStatus == PaymentStatus.PAID) {
            // 1. Chỉ cập nhật đúng trạng thái của Booking
            booking.setPaymentStatus(PaymentStatus.PAID);
            booking.setStatus(BookingStatus.CONFIRMED);
            bookingRepository.save(booking);

            // 2. Phát loa thông báo: "Đơn này đã trả tiền xong!"
            log.info("[IPN SUCCESS] Đơn hàng {} chốt PAID. Kích hoạt luồng hậu thanh toán...", bookingCode);
            eventPublisher.publishEvent(new BookingPaidEvent(booking));


            return strategy.buildIPNSuccessResponse("Xác nhận đơn thành công");
        } else {
            booking.setPaymentStatus(PaymentStatus.UNPAID);
            bookingRepository.save(booking);
            return strategy.buildIPNSuccessResponse("Giao dịch gốc thất bại");
        }
    }
    // BƯỚC 1: Tạo lệnh thiết lập thẻ nháp trên Stripe
    @Override
    public String createSetupIntent(Long userId) {
        try {
            SetupIntentCreateParams params = SetupIntentCreateParams.builder()
                    .setUsage(SetupIntentCreateParams.Usage.OFF_SESSION) // Dùng để tự động trừ tiền sau này (Auto-renew)
                    .build();

            SetupIntent setupIntent = SetupIntent.create(params);
            return setupIntent.getClientSecret(); // Trả cái này về cho Angular bọc form
        } catch (Exception e) {
            throw new RuntimeException("Không thể khởi tạo lệnh liên kết thẻ trên Stripe", e);
        }
    }

    // BƯỚC 5: Hứng Token từ FE gửi về, tự bóc tách thông tin thẻ từ Stripe rồi lưu DB
    @Override
    @Transactional
    public void savePaymentMethod(Long userId, String paymentMethodId) {
        try {
            // 1. Gọi sang Stripe để lôi thông tin thô của thẻ ra check
            PaymentMethod stripeMethod = PaymentMethod.retrieve(paymentMethodId);
            PaymentMethod.Card cardInfo = stripeMethod.getCard();

            if (cardInfo == null) throw new RuntimeException("Phương thức này không phải là thẻ tín dụng");

            // 2. TÌM HOẶC TẠO STRIPE CUSTOMER (Giải pháp cứu cánh thẻ vô gia cư)
            List<UserPaymentMethod> existingCards = paymentMethodRepository.findByUserIdOrderByIsPrimaryDesc(userId);
            String stripeCustomerId = existingCards.stream()
                    .map(UserPaymentMethod::getStripeCustomerId)
                    .filter(java.util.Objects::nonNull)
                    .findFirst()
                    .orElse(null);

            if (stripeCustomerId == null) {
                // Nếu User này chưa từng liên kết thẻ nào -> Tạo mới một Customer trên hệ thống Stripe
                Map<String, Object> customerParams = new HashMap<>();
                customerParams.put("metadata", Map.of("userId", userId.toString()));
                // Ông có thể đút thêm email khách vào đây nếu muốn: customerParams.put("email", "abc@gmail.com");
                com.stripe.model.Customer stripeCustomer = com.stripe.model.Customer.create(customerParams);
                stripeCustomerId = stripeCustomer.getId(); // Lấy được mã cus_xxxx
            }

            // 3. BẮT BUỘC: Ra lệnh trói chặt PaymentMethod vào Customer này trên Stripe
            com.stripe.param.PaymentMethodAttachParams attachParams = com.stripe.param.PaymentMethodAttachParams.builder()
                    .setCustomer(stripeCustomerId)
                    .build();
            stripeMethod.attach(attachParams); // Thẻ chính thức có chủ sở hữu trên Stripe!

            // 4. Tạo Entity lưu vào DB nhà mình
            UserPaymentMethod entity = new UserPaymentMethod();
            entity.setUserId(userId);
            entity.setProvider("STRIPE");
            entity.setGatewayToken(paymentMethodId);
            entity.setStripeCustomerId(stripeCustomerId); // <<< LƯU LẠI CUS_XXXX VÀO DB
            entity.setCardBrand(cardInfo.getBrand());
            entity.setCardType(cardInfo.getFunding());
            entity.setLastFour(cardInfo.getLast4());
            entity.setExpMonth(cardInfo.getExpMonth().intValue());
            entity.setExpYear(cardInfo.getExpYear().intValue());
            entity.setCardHolderName(stripeMethod.getBillingDetails().getName() != null ?
                    stripeMethod.getBillingDetails().getName() : "UNKNOWN HOLDER");

            boolean hasCards = !existingCards.isEmpty();
            entity.setIsPrimary(!hasCards);

            paymentMethodRepository.save(entity);
            log.info("✔ Đã trói thẻ và lưu thành công Customer ID: {} cho user: {}", stripeCustomerId, userId);

        } catch (Exception e) {
            log.error("Lỗi liên kết thẻ: {}", e.getMessage());
            throw new RuntimeException("Lưu phương thức thanh toán thất bại: " + e.getMessage());
        }
    }
    @Override
    @Transactional(readOnly = true) // Thêm readOnly để tối ưu tốc độ câu SELECT của Hibernate
    public List<UserPaymentMethodResponse> getPaymentMethodsByUserId(Long userId) {
        log.info("Đang lấy danh sách thẻ liên kết cho user: {}", userId);

        List<UserPaymentMethod> entities = paymentMethodRepository.findByUserIdOrderByIsPrimaryDesc(userId);

        return entities.stream()
                .map(this::mapToResponse)
                .toList(); // Cách viết gọn của Java 16+ thay cho .collect(Collectors.toList())
    }

    private UserPaymentMethodResponse mapToResponse(UserPaymentMethod entity) {
        return UserPaymentMethodResponse.builder()
                .id(entity.getId())
                .userId(entity.getUserId())
                .provider(entity.getProvider())
                .cardBrand(entity.getCardBrand())
                .cardType(entity.getCardType())
                .lastFour(entity.getLastFour())
                .expMonth(entity.getExpMonth())
                .expYear(entity.getExpYear())
                .cardHolderName(entity.getCardHolderName())
                .isPrimary(entity.getIsPrimary())
                .status(entity.getStatus())
                .build();
    }
    /**
     * API Xác nhận thanh toán trung tâm từ trang Checkout (Độ thêm luồng Quick Pay bằng thẻ Stripe)
     */
    @Override
    @Transactional
    public clyvasync.Clyvasync.dto.response.PaymentConfirmResponse processCheckoutPayment(Long userId, clyvasync.Clyvasync.dto.request.PaymentConfirmRequest request, HttpServletRequest httpServletRequest) {
        String methodStr = request.getPaymentMethod();
        String bookingCode = request.getBookingCode();

        // 1. Lấy thông tin Booking từ DB lên
        Booking booking = bookingRepository.findBookingByBookingCode(bookingCode)
                .orElseThrow(() -> new AppException(ResultCode.BOOKING_NOT_FOUND));

        log.info("[CHECKOUT COMPONENT] Khách hàng {} yêu cầu thanh toán đơn hàng #{} qua: {}", userId, booking.getBookingCode(), methodStr);

        // =================================================================
        // 🚀 BƯỚC MỚI ĐỘ: VẬN HÀNH BỘ LỌC BẢO VỆ (VALIDATE TRẠNG THÁI ĐƠN HÀNG)
        // =================================================================
        validateBookingState(booking);

        // =================================================================
        // TRƯỜNG HỢP 1: QUICK PAY QUA THẺ TÍN DỤNG ĐÃ LIÊN KẾT (STRIPE)
        // =================================================================
        if (methodStr.startsWith("CARD_")) {
            try {
                Long cardId = Long.parseLong(methodStr.substring(5));

                UserPaymentMethod card = paymentMethodRepository.findById(cardId)
                        .orElseThrow(() -> new AppException(ResultCode.PAYMENT_METHOD_NOT_FOUND));

                if (!card.getUserId().equals(userId)) {
                    throw new RuntimeException("Bạn không có quyền sử dụng phương thức thanh toán này!");
                }

                long amountInCents = booking.getTotalPrice().multiply(new BigDecimal(100)).longValue();

                com.stripe.param.PaymentIntentCreateParams params = com.stripe.param.PaymentIntentCreateParams.builder()
                        .setAmount(amountInCents)
                        .setCurrency("usd")
                        .setPaymentMethod(card.getGatewayToken())
                        .setCustomer(card.getStripeCustomerId())
                        .setConfirm(true)
                        .setOffSession(true)
                        .build();

                com.stripe.model.PaymentIntent intent = com.stripe.model.PaymentIntent.create(params);

                if ("succeeded".equals(intent.getStatus())) {
                    booking.setPaymentStatus(PaymentStatus.PAID);
                    booking.setStatus(BookingStatus.CONFIRMED);
                    bookingRepository.save(booking);

                    log.info("[STRIPE QUICK PAY SUCCESS] Đơn hàng {} đã thanh toán thành công ngầm.", booking.getBookingCode());
                    eventPublisher.publishEvent(new BookingPaidEvent(booking));

                    return clyvasync.Clyvasync.dto.response.PaymentConfirmResponse.builder()
                            .status("SUCCEEDED")
                            .message("Thanh toán Quick Pay thành công qua thẻ " + card.getCardBrand().toUpperCase())
                            .build();
                } else {
                    throw new RuntimeException("Giao dịch bị từ chối bởi Ngân hàng phát hành.");
                }

            } catch (Exception e) {
                log.error("❌ Thất bại khi quẹt thẻ Quick Pay: {}", e.getMessage());
                throw new AppException(ResultCode.PAYMENT_FAILED_OR_CANCELLED);
            }
        }

        // =================================================================
        // TRƯỜNG HỢP 2: CỔNG THANH TOÁN ONLINE CẦN RENDER URL (VNPAY / MOMO)
        // =================================================================
        if ("VNPAY".equals(methodStr) || "MOMO".equals(methodStr)) {
            clyvasync.Clyvasync.enums.payment.PaymentMethod pMethod = clyvasync.Clyvasync.enums.payment.PaymentMethod.valueOf(methodStr);
            PaymentStrategy strategy = paymentFactory.getStrategy(pMethod);

            String redirectUrl = strategy.createPaymentUrl(booking, httpServletRequest);

            return clyvasync.Clyvasync.dto.response.PaymentConfirmResponse.builder()
                    .status("REDIRECT")
                    .redirectUrl(redirectUrl)
                    .message("Khởi tạo link liên kết cổng " + methodStr + " thành công")
                    .build();
        }

        // =================================================================
        // TRƯỜNG HỢP 3: CHUYỂN KHOẢN THỦ CÔNG
        // =================================================================
        if ("TRANSFER".equals(methodStr)) {
            booking.setPaymentStatus(PaymentStatus.UNPAID);
            booking.setStatus(BookingStatus.PENDING);
            bookingRepository.save(booking);

            return clyvasync.Clyvasync.dto.response.PaymentConfirmResponse.builder()
                    .status("PENDING")
                    .message("Hệ thống đã ghi nhận. Vui lòng chuyển khoản đúng cú pháp để được phê duyệt đặt chỗ.")
                    .build();
        }

        throw new AppException(ResultCode.INVALID_PAYMENT_METHOD);
    }

    /**
     * HÀM BỔ TRỢ: Quét sạch các kịch bản lỗi trạng thái của Đơn hàng trước khi thanh toán
     */
    private void validateBookingState(Booking booking) {
        // Kịch bản 1: Đơn này đã trả tiền xong xuôi từ trước rồi (Chống trùng lặp tiền bạc)
        if (PaymentStatus.PAID.equals(booking.getPaymentStatus()) || BookingStatus.CONFIRMED.equals(booking.getStatus())) {
            throw new AppException(ResultCode.BOOKING_ALREADY_PAID);
        }

        // Kịch bản 2: Đơn đã bị hủy bỏ hoặc bị từ chối trước đó (Không cho phép nạp tiền cho đơn chết)
        if (BookingStatus.CANCELLED.equals(booking.getStatus())) { // Ông check lại đúng tên enum CANCELLED bên ông nhé
            throw new AppException(ResultCode.BOOKING_ALREADY_CANCELLED);
        }

        // Kịch bản 3: Phòng/Chuyến đi đã diễn ra hoặc đã kết thúc trong quá khứ (Quá hạn thanh toán)
        // Tận dụng chính bookingDetailService có sẵn trong file của ông để bóc ngày check-out
        BookingDetail detail = bookingDetailService.findBookingDetailByBookingId(booking.getId());
        if (detail != null && detail.getCheckOutDate() != null) {
            java.time.LocalDate today = java.time.LocalDate.now();
            if (detail.getCheckOutDate().isBefore(today)) {
                throw new AppException(ResultCode.BOOKING_EXPIRED_OR_ENDED);
            }
        }
    }
    @Override
    @Transactional(readOnly = true)
    public Map<String, Object> getPaymentSuccessDetails(String bookingCode) {
        Booking booking = bookingService.getBookingByCode(bookingCode);

        // Bảo mật check: Chỉ cho phép xem nếu đơn này thực sự đã được thanh toán thành công
        if (!PaymentStatus.PAID.equals(booking.getPaymentStatus())) {
            throw new AppException(ResultCode.PAYMENT_FAILED_OR_CANCELLED);
        }

        Map<String, Object> data = new HashMap<>();
        data.put("bookingCode", booking.getBookingCode());
        data.put("totalPrice", booking.getTotalPrice());
        data.put("guestName", booking.getGuestName());

        // Tái sử dụng logic cào dữ liệu phòng của ông
        BookingDetail detail = bookingDetailService.findBookingDetailByBookingId(booking.getId());
        if (detail != null) {
            HomestayRoom room = homestayRoomService.getRoomById(detail.getRoomId());
            data.put("roomName", room != null ? room.getName() : "N/A");
            data.put("guests", detail.getGuestCount() + " Guests");
        }

        // Tái sử dụng logic cào danh sách tour đính kèm của ông
        List<TourBooking> tourBookings = tourBookingService.findAllByHomestayBookingId(booking.getId());
        data.put("tours", tourBookings);

        return data;
    }
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void deletePaymentMethod(Long userId, Long cardId) {
        UserPaymentMethod card = paymentMethodRepository.findById(cardId)
                .orElseThrow(() -> new AppException(ResultCode.PAYMENT_METHOD_NOT_FOUND));

        if (!card.getUserId().equals(userId)) {
            throw new AppException(ResultCode.PERMISSION_DENIED);
        }

        try {
            com.stripe.model.PaymentMethod stripeMethod = com.stripe.model.PaymentMethod.retrieve(card.getGatewayToken());
            stripeMethod.detach();
            log.info("[STRIPE DETACH SUCCESS] Đã gỡ thẻ {} khỏi hệ thống Stripe", card.getGatewayToken());

            boolean wasPrimary = card.getIsPrimary();

            paymentMethodRepository.delete(card);
            log.info("[DB DELETE SUCCESS] Đã xóa thẻ ID #{} khỏi hệ thống.", cardId);

            if (wasPrimary) {
                List<UserPaymentMethod> remainingCards = paymentMethodRepository.findByUserIdOrderByIdAsc(userId);

                if (!remainingCards.isEmpty()) {
                    UserPaymentMethod newPrimaryCard = remainingCards.get(0);
                    newPrimaryCard.setIsPrimary(true);
                    paymentMethodRepository.save(newPrimaryCard);
                    log.info("[AUTO PRIMARY SET] Thẻ ID #{} đã được đôn lên làm mặc định thay thế.", newPrimaryCard.getId());
                }
            }

        } catch (Exception e) {
            log.error("❌ Thất bại khi gỡ liên kết thẻ Stripe: {}", e.getMessage());
            throw new AppException(ResultCode.PAYMENT_METHOD_SAVE_FAILED);
        }
    }
}
