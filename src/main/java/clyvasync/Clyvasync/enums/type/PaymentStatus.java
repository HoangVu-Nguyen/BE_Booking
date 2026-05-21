package clyvasync.Clyvasync.enums.type;

public enum PaymentStatus {
    UNPAID,
    PAID,             // VNPAY/MoMo báo thành công
    REFUNDING,        // Đang gửi request hoàn tiền qua API MoMo/VNPAY (Khi khách hủy)
    REFUNDED          // Hoàn tiền thành công
}