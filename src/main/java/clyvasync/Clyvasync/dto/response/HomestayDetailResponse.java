package clyvasync.Clyvasync.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HomestayDetailResponse {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String city;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private BigDecimal basePrice;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private List<String> imageUrls;
    private List<AmenityResponse> amenities;
    private Long ownerId;
    private List<ReviewResponse> reviews;
}