package clyvasync.Clyvasync.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TripResponse {
    private String bookingCode;
    private String propertyName;
    private String location;
    private String propertyImage;

    private LocalDate checkIn;
    private LocalDate checkOut;
    private int totalGuests;
    private BigDecimal totalPrice;
    private String status;
    private List<TripTourResponse> tours;
}