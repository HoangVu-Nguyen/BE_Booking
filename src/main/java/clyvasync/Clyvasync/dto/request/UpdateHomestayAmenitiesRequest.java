package clyvasync.Clyvasync.dto.request;

import lombok.Data;

import java.util.Set;

@Data
public class UpdateHomestayAmenitiesRequest {
    private Set<Long> amenityIds;
}
