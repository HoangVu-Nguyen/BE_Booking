package clyvasync.Clyvasync.service.homestay;

import java.util.List;
import java.util.Map;

public interface CategoryService {
    Map<Integer, String> getCategoryNamesMap(List<Integer> categoryIds);
}
