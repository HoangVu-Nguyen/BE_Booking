package clyvasync.Clyvasync.constant;

public class MessagingConstants {
    public static final String REGISTER_EXCHANGE = "x.register.user";
    public static final String EMAIL_QUEUE = "q.register.send-email";
    public static final String REGISTER_ROUTING_KEY = "r.register.auth";
    // --- Lĩnh vực Đặt phòng (Booking / Payment) ---
    public static final String BOOKING_EXCHANGE = "x.booking.payment";
    public static final String PAYMENT_MAIL_QUEUE = "q.booking.send-payment-mail";
    public static final String PAYMENT_MAIL_ROUTING_KEY = "r.booking.payment-mail";
}