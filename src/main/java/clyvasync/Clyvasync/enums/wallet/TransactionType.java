package clyvasync.Clyvasync.enums.wallet;

public enum TransactionType {
    BOOKING_REVENUE,    // Tiền ký quỹ từ khách đặt phòng
    ESCROW_RELEASE,     // Giải ngân từ tiền giam sang tiền khả dụng
    WITHDRAWAL,         // Lệnh rút tiền về ngân hàng của Host
    REFUND_DEDUCTION    // Trừ tiền khi khách hủy/khiếu nại
}