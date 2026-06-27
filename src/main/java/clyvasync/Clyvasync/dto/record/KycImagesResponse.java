package clyvasync.Clyvasync.dto.record;

import java.util.List;

public record KycImagesResponse(
        Long profileId,
        List<PresignedUrlResponse> images
) {}
