package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.enums.offer.DiscountType;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserVoucherResponse {
    private Long id;
    private String code;
    private String title;
    private BigDecimal discountValue;
    private DiscountType discountType;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private OffsetDateTime validUntil;
    
    private String status;
}
