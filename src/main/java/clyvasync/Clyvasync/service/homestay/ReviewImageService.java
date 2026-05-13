package clyvasync.Clyvasync.service.homestay;

import java.util.List;
import java.util.Map;

public interface ReviewImageService {
    Map<Long, List<String>> getImagesForReviews(List<Long> reviewIds);

}
