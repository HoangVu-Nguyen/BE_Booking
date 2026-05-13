package clyvasync.Clyvasync.repository.projection;

import clyvasync.Clyvasync.modules.homestay.entity.Amenity;

public interface AmenityBatchProjection {
    Long getHomestayId();
    Amenity getAmenity();
}