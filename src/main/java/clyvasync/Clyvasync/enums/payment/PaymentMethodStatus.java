package clyvasync.Clyvasync.enums.payment;

public enum PaymentMethodStatus {
    ACTIVE,    // Thẻ đang hoạt động bình thường
    EXPIRED,   // Thẻ đã hết hạn dựa theo tháng/năm
    LOCKED     // Thẻ bị khóa tạm thời (khi user bấm Lock Card trên UI)
}