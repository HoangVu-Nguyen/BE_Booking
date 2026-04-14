package clyvasync.Clyvasync.dto.request;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
public class HomestayRequest {
    private String name;
    private String description;
    private String address;
    private String city;
    private BigDecimal basePrice;
    private Integer maxGuests;
    private Integer numBedrooms;
    private Integer numBathrooms;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private Set<Long> amenityIds;
    private List<String> imageUrls;
}