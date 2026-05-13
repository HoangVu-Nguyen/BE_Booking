package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.modules.homestay.entity.Location;
import clyvasync.Clyvasync.repository.homestay.LocationRepository;
import clyvasync.Clyvasync.service.homestay.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Repository
@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {
    private final LocationRepository locationRepository;

    @Override
    public Map<Integer, String> getLocationNamesMap(List<Integer> locationIds) {
        return locationRepository.findAllByIdIn(locationIds).stream()
                .collect(Collectors.toMap(Location::getId, Location::getCityName));
    }
}