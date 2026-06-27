package clyvasync.Clyvasync.enums.kyc;

public enum KycDocumentStatus {
    PENDING,    // Chờ duyệt
    SUBMITTED, // đã nộp và chờ xác thực
    VERIFIED,   // Hợp lệ
    REJECTED    // Bị mờ/sai (cần upload lại)
}
