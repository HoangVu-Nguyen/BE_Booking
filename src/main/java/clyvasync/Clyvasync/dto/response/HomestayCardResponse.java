package clyvasync.Clyvasync.dto.response;


import clyvasync.Clyvasync.enums.homestay.HomestayStatus;
import lombok.*;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HomestayCardResponse {
    private Long id;
    private String name;
    private String cityName;
    private BigDecimal basePrice;
    private HomestayStatus status;

    private List<String> imageUrls;

    // Phục vụ render badge rating nhanh
    private BigDecimal averageRating;

    // Cờ sinh tử để bật/tắt tim đỏ
    private Boolean isFavorite;
}