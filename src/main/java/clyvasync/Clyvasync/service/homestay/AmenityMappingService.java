package clyvasync.Clyvasync.service.homestay;

import clyvasync.Clyvasync.dto.record.AmenityMappingResult;

import java.util.List;

public interface AmenityMappingService {
    AmenityMappingResult processAiAmenities(List<String> aiKeywords);
}
