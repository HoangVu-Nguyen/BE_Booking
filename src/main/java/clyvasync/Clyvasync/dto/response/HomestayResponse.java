package clyvasync.Clyvasync.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

@Data
public class HomestayResponse {
    private Long id;
    private String name;
    private String description;
    private String address;
    private String city;
    private BigDecimal basePrice;
    private Integer maxGuests;
    private Integer numBedrooms;
    private Integer numBathrooms;
    private String status;
    private Long ownerId;
    private BigDecimal averageRating;
    private Integer reviewCount;
    private List<String> images;
    private Set<Long> amenityIds;
}