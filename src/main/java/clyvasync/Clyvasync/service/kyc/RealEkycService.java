package clyvasync.Clyvasync.service.kyc;

import java.util.List;

public interface RealEkycService {
    void processEkyc(Long profileId, List<String> imageUrls);
}
