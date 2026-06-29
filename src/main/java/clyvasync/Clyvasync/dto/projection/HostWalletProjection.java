package clyvasync.Clyvasync.dto.projection;

import java.math.BigDecimal;

public interface HostWalletProjection {
    Long getOwnerId();
    BigDecimal getBalance();
}
