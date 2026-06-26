package clyvasync.Clyvasync.dto.projection;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;

public record LedgerTransactionProjection(
        Long txnId,
        Timestamp txnDate,
        String type,
        String status,
        BigDecimal txnAmount,
        String bookingCode,
        BigDecimal gross,
        BigDecimal fee,
        BigDecimal net,
        String guestName,
        String guestAvatar,
        String hostName,
        String bankInfo
) {}