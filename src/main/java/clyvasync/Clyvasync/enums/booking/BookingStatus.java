package clyvasync.Clyvasync.enums.booking;


public enum BookingStatus {
    DRAFT,            // Khách mới vào trang checkout
    PENDING,           //Chờ duyệt từ host
    AWAITING_PAYMENT, // Host ĐÃ DUYỆT -> Chờ khách thanh toán
    PENDING_PAYMENT,  // Đang chờ Gateway (VNPAY/MoMo) phản hồi
    CONFIRMED,        // Đã thanh toán, phòng đã bị trừ (Available_quantity - 1)

    // --- CÁC TRẠNG THÁI MỚI THÊM VÀO ---
    CHECKED_IN,       // Khách đã nhận phòng (Bắt đầu đếm ngược 24h khiếu nại)
    COMPLETED,        // Đã trả phòng an toàn (Kết thúc booking)
    DISPUTE,          // CẢNH BÁO: Khách đang khiếu nại, đóng băng giải ngân

    CANCELLED,        // Khách/Host hủy hoặc hết hạn thanh toán
    FAILED            // Lỗi thanh toán
}