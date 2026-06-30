package clyvasync.Clyvasync.dto.projection;

import java.math.BigDecimal;

public interface HostFinancialProjection {
    Long getOwnerId();
    BigDecimal getGmv();
    BigDecimal getPlatformFee();
    Long getTotalBookingsAllStatus();
    Long getCompletedBookings();
    Long getCancelledBookings();
}