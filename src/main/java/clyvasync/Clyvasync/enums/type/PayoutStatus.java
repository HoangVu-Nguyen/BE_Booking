package clyvasync.Clyvasync.enums.type;

public enum PayoutStatus {
    NOT_APPLICABLE,   // Trạng thái mặc định khi booking chưa PAID
    ON_HOLD,          // Tiền đang bị giam (Nằm ở pending_balance của Host)
    ELIGIBLE,         // Đã qua 24h an toàn (Cronjob quét và chuyển tiền sang available_balance)
    PAID_OUT          // Tiền đã được admin chuyển/rút về ngân hàng thực tế của Host
}