package clyvasync.Clyvasync.enums.homestay;

public enum HomestayStatus {
    DRAFT,                 // Nháp, chưa gửi duyệt
    PENDING_VERIFICATION,  // Đang chờ Admin duyệt giấy tờ
    APPROVED,              // Đã duyệt, đủ điều kiện hoạt động
    REJECTED,              // Bị từ chối
    HIDDEN                 // Host chủ động ẩn đi (không cho thuê nữa hoặc tạm nghỉ)
}