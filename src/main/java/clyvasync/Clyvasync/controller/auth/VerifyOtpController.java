package clyvasync.Clyvasync.controller.auth;

import clyvasync.Clyvasync.dto.request.ResendVerificationRequest;
import clyvasync.Clyvasync.dto.request.VerifyAccountRequest;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.service.auth.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
@Slf4j
public class VerifyOtpController {

    private final AuthService authService;

    // Hiển thị giao diện OTP
    @GetMapping("/verify-otp")
    public String showVerifyPage(@RequestParam String email, Model model) {
        model.addAttribute("email", email);
        return "verify-otp";
    }

    // Xử lý nút A (CONFIRM)
    @PostMapping("/verify-otp")
    public String verifyAccount(@RequestParam String email,
                                @RequestParam String code,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        try {
            // Khởi tạo request theo cấu trúc của bạn
            VerifyAccountRequest request = new VerifyAccountRequest();
            request.setEmail(email);
            request.setCode(code);

            // Gọi hàm xử lý đã có trong Service
            authService.verifyAccount(request);

            // Xác thực thành công -> Đá về trang Login
            redirectAttributes.addAttribute("success", "STAGE CLEARED! Vui lòng đăng nhập.");
            return "redirect:/login";

        } catch (AppException e) {
            log.warn("Lỗi OTP: {}", e.getResultCode().name());
            model.addAttribute("error", e.getResultCode().name());
            model.addAttribute("email", email);
            return "verify-otp";
        }
    }

    // Xử lý nút RESEND LIFE (1UP)
    @PostMapping("/resend-otp")
    public String resendOtp(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            ResendVerificationRequest request = new ResendVerificationRequest();
            request.setEmail(email);
            authService.resendVerification(request);

            redirectAttributes.addFlashAttribute("success", "1UP! Đã gửi mã bí mật mới vào hòm thư.");
        } catch (AppException e) {
            redirectAttributes.addFlashAttribute("error", e.getResultCode().name());
        }

        // SỬA DÒNG NÀY TƯƠNG TỰ
        redirectAttributes.addAttribute("email", email);
        return "redirect:/verify-otp";
    }
}