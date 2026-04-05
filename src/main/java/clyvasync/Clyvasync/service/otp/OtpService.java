package clyvasync.Clyvasync.service.otp;

public interface OtpService {
    String generateOtp(); // Tạo mã OTP ngẫu nhiên
    boolean validateOtpFormat(String otp); // (Tùy chọn) Kiểm tra định dạng OTP
}
