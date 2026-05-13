package clyvasync.Clyvasync.service.homestay;

import java.util.List;
import java.util.Map;

public interface LocationService {
    Map<Integer, String> getLocationNamesMap(List<Integer> locationIds);

}
