package clyvasync.Clyvasync.service.trip;

import clyvasync.Clyvasync.dto.response.TripResponse;

import java.util.List;

public interface TripService {
    List<TripResponse> getUserTrips(Long userId);
}
