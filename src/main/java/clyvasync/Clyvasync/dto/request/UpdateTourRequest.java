package clyvasync.Clyvasync.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record UpdateTourRequest(
        String name,
        String description,
        clyvasync.Clyvasync.enums.type.DurationType durationType,
        @Min(1) Integer durationValue,
        @Min(0) BigDecimal pricePerPerson,
        @Min(1) Integer maxParticipants,
        Boolean allowExternalGuests,
        java.util.List<String> imageKeys
) {}
