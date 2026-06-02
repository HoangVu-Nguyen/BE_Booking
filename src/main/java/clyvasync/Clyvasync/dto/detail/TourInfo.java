package clyvasync.Clyvasync.dto.detail;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class TourInfo {
    private String tourName;
    private String tourDate;
    private BigDecimal price;
}