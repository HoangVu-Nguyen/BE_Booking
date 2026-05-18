package clyvasync.Clyvasync.service.homestay;

import clyvasync.Clyvasync.modules.homestay.entity.HomestayPolicy;

import java.util.List;

public interface HomestayPolicyService {
    HomestayPolicy getHomestayPolicyByHomestayId(Long homestayId);
    List<HomestayPolicy> findAllByHomestayId(Long homestayId);

}
