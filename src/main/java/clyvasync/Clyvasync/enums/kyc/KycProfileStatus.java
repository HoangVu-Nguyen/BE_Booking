package clyvasync.Clyvasync.enums.kyc;

public enum KycProfileStatus {
    PENDING_REVIEW, // Chờ duyệt
    PENDING_APPROVAL,
    APPROVED,       // Đã duyệt
    REJECTED,       // Bị từ chối (yêu cầu làm lại)
    BANNED          // Khóa vĩnh viễn (Phát hiện giả mạo)
}