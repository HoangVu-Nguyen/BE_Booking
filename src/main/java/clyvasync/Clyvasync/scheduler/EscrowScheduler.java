package clyvasync.Clyvasync.scheduler;

import clyvasync.Clyvasync.modules.booking.entity.Booking;
import clyvasync.Clyvasync.repository.booking.BookingRepository;
import clyvasync.Clyvasync.service.booking.BookingService;
import clyvasync.Clyvasync.service.wallet.HostWalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EscrowScheduler {

    private final BookingService bookingService;
    private final HostWalletService hostWalletService;

    /**
     * Chạy tự động vào PHÚT THỨ 0 CỦA MỖI GIỜ (Ví dụ: 13:00, 14:00, 15:00...)
     * Biểu thức Cron: "0 0 * * * *"
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void processEscrowRelease() {
        log.info("[ESCROW SCHEDULER] Bắt đầu quét các booking đến hạn giải ngân...");

        // Tính mốc thời gian: Lấy ngày hôm qua (để đảm bảo đã qua 24h từ lúc check-in hôm qua)
        LocalDate targetDate = LocalDate.now().minusDays(1);

        // Lấy danh sách các đơn đạt chuẩn
        List<Booking> eligibleBookings = bookingService.findBookingsReadyForEscrowRelease(targetDate);

        if (eligibleBookings.isEmpty()) {
            log.info("[ESCROW SCHEDULER] Không có booking nào cần giải ngân lúc này.");
            return;
        }

        int successCount = 0;
        for (Booking booking : eligibleBookings) {
            try {
                // Gọi hàm nhả tiền từ Pending sang Available mà bác đã viết hôm trước
                hostWalletService.releaseEscrowFunds(booking.getId());
                successCount++;
                log.info("Đã tự động giải ngân cho Booking ID: {} | Mã: {}", booking.getId(), booking.getBookingCode());
            } catch (Exception e) {
                // Bắt try-catch ở đây để nếu 1 đơn bị lỗi DB, các đơn khác vẫn được giải ngân bình thường
                log.error("Lỗi khi giải ngân cho Booking ID: {}", booking.getId(), e);
            }
        }

        log.info("[ESCROW SCHEDULER] Hoàn tất quét. Đã giải ngân thành công {}/{} đơn.", successCount, eligibleBookings.size());
    }
}