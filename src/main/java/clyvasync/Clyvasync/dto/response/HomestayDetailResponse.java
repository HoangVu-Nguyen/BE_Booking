package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.enums.homestay.HomestayStatus;
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
    private String addressDetail;
    private BigDecimal basePrice;

    // Thông tin quy mô
    private Integer maxGuests;
    private Integer numBedrooms;
    private Integer numBathrooms;

    private Double latitude;
    private Double longitude;

    private String categoryName;
    private String cityName;
    private HomestayStatus status;

    private List<String> imageUrls;

    private List<AmenityResponse> amenities;

    private OwnerResponse owner;

    private BigDecimal averageRating;
    private Integer reviewCount;
    private List<ReviewResponse> reviews;
    private List<TourResponse>  tours;
    private List<RoomResponse> rooms;
}