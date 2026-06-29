package clyvasync.Clyvasync.dto.projection;

import java.math.BigDecimal;

public interface HomestayFinancialProjection {
    Long getHomestayId();
    Long getTotalBookingsAllStatus(); // Tổng tất cả đơn
    Long getCompletedBookings();      // Đơn thành công
    Long getCancelledBookings();      // Đơn bị hủy
    BigDecimal getTotalRevenue();     // Doanh thu (chỉ tính đơn thành công)
}