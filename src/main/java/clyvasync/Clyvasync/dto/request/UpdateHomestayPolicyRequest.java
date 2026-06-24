package clyvasync.Clyvasync.dto.request;


import clyvasync.Clyvasync.enums.booking.BookingMode;
import clyvasync.Clyvasync.enums.room.CancellationPolicy;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class UpdateHomestayPolicyRequest {

    private LocalTime checkInFrom;
    private LocalTime checkInTo;
    private LocalTime checkOutBefore;

    @Min(1)
    private Integer minNights;

    @Min(1)
    private Integer maxNights;

    private BookingMode bookingMode;
    private CancellationPolicy cancellationPolicy;

    private Boolean childrenAllowed;
    private Boolean petsAllowed;
    private Boolean smokingAllowed;
    private Boolean partyAllowed;

    private Boolean quietHoursEnabled;
    private LocalTime quietFrom;
    private LocalTime quietTo;

    private Boolean depositRequired;

    @DecimalMin("0.0")
    private BigDecimal depositAmount;

    @Size(max = 1000)
    private String lateCheckInInstruction;

    @Size(max = 1000)
    private String extraNotes;
}