package clyvasync.Clyvasync.dto.request;


import clyvasync.Clyvasync.enums.booking.BookingMode;
import clyvasync.Clyvasync.enums.room.CancellationPolicy;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalTime;

@Data
public class UpdateRoomPolicyRequest {

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkInFrom;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkInTo;

    @JsonFormat(pattern = "HH:mm")
    private LocalTime checkOutBefore;

    @Min(value = 1, message = "Số đêm tối thiểu phải lớn hơn hoặc bằng 1")
    private Integer minNights;

    @Min(value = 1, message = "Số đêm tối đa phải lớn hơn hoặc bằng 1")
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

    @DecimalMin(value = "0.0", inclusive = true, message = "Tiền cọc không được âm")
    private BigDecimal depositAmount;

    @Size(max = 1000, message = "Ghi chú không được vượt quá 1000 ký tự")
    private String extraNotes;
}
