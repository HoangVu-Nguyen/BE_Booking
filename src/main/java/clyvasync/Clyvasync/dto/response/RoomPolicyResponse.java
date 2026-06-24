package clyvasync.Clyvasync.dto.response;


import clyvasync.Clyvasync.enums.booking.BookingMode;
import clyvasync.Clyvasync.enums.room.CancellationPolicy;
import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
public class RoomPolicyResponse {

    private Long id;

    private Long homestayId;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkInFrom;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkInTo;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkOutBefore;

    private Integer minNights;

    private Integer maxNights;

    private BookingMode bookingMode;

    private CancellationPolicy cancellationPolicy;

    private Boolean childrenAllowed;

    private Boolean petsAllowed;

    private Boolean smokingAllowed;

    private Boolean partyAllowed;

    private Boolean quietHoursEnabled;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime quietFrom;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime quietTo;

    private Boolean depositRequired;

    private BigDecimal depositAmount;

    private String extraNotes;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
