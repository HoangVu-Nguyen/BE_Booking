package clyvasync.Clyvasync.constant;

public class MessagingConstants {
    public static final String REGISTER_EXCHANGE = "x.register.user";
    public static final String EMAIL_QUEUE = "q.register.send-email";
    public static final String REGISTER_ROUTING_KEY = "r.register.auth";
    // --- Lĩnh vực Đặt phòng (Booking / Payment) ---
    public static final String BOOKING_EXCHANGE = "x.booking.payment";
    public static final String PAYMENT_MAIL_QUEUE = "q.booking.send-payment-mail";
    public static final String PAYMENT_MAIL_ROUTING_KEY = "r.booking.payment-mail";
    // ==========================================
    // --- Lĩnh vực Định danh (KYC) ---
    // ==========================================
    // Exchange xử lý mọi sự kiện liên quan đến hồ sơ KYC
    public static final String KYC_EXCHANGE = "x.kyc.events";

    // Queue dành cho Worker chạy ngầm (gọi API eKYC bên thứ 3)
    public static final String KYC_PROCESS_EKYC_QUEUE = "q.kyc.process-ekyc";

    // Routing key khi Host vừa nộp hồ sơ thành công
    public static final String KYC_SUBMITTED_ROUTING_KEY = "r.kyc.submitted";
}