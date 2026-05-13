package clyvasync.Clyvasync.service.homestay;

import java.util.List;
import java.util.Map;

public interface HomestayImageService {
    Map<Long, List<String>> getImagesForHomestays(List<Long> homestayIds);
    List<String> getImagesByHomestayId(Long homestayId);
}
