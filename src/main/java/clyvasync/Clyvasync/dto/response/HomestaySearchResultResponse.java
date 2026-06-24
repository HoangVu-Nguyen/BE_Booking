package clyvasync.Clyvasync.dto.response;

import java.math.BigDecimal;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class HomestaySearchResultResponse {
    private Long roomId;
    private Long homestayId;
    private String name;        // Tên phòng
    private String city;
    private BigDecimal price;   // Giá hiện tại
    private Integer bedCount;
    private Integer maxGuests;
    private Double matchScore;
}
