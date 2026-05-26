package clyvasync.Clyvasync.enums.type;

public enum NotificationType {
    // Nhóm Booking
    BOOKING_CREATED,
    BOOKING_CANCELLED,
    BOOKING_CONFIRMED,
    BOOKING_REQUEST,

    BOOKING_AWAITING_PAYMENT,

    // Nhóm Payment
    PAYMENT_SUCCESS,
    PAYMENT_FAILED,

    // Nhóm System / Host
    REVIEW_RECEIVED,
    SYSTEM_ALERT,
    PROMOTION
}