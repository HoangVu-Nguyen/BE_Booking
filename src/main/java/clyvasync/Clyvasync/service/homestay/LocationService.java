package clyvasync.Clyvasync.service.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.Location;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface LocationService {
    Map<Integer, String> getLocationNamesMap(List<Integer> locationIds);
    List<Location> findAllByIds(List<Integer> ids);
    Optional<Integer> findIdByNameOrSlug(String nameOrSlug);

}
