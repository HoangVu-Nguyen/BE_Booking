package clyvasync.Clyvasync.enums.wallet;

public enum TransactionStatus {
    PENDING,    // Chờ xử lý (áp dụng cho lệnh rút tiền mới tạo hoặc doanh thu đang giam)
    PROCESSING, // Kế toán đang xử lý chuyển khoản (optional)
    COMPLETED,  // Giao dịch thành công
    FAILED      // Giao dịch thất bại / Bị Admin từ chối
}