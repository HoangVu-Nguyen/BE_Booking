package clyvasync.Clyvasync.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record UpdateTourRequest(
        @NotBlank(message = "Tên tour không được để trống")
        String name,
        String description,
        @Min(0) BigDecimal pricePerPerson,
        @Min(1) Integer maxParticipants,
        Boolean allowExternalGuests
) {}
