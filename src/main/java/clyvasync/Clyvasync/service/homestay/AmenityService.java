package clyvasync.Clyvasync.service.homestay;

import clyvasync.Clyvasync.dto.response.AmenityResponse;

import java.util.List;
import java.util.Map;

public interface AmenityService {
    List<AmenityResponse> getAmenitiesByHomestayId(Long homestayId);

    Map<Long, List<AmenityResponse>> getAmenitiesForHomestays(List<Long> homestayIds);
}
