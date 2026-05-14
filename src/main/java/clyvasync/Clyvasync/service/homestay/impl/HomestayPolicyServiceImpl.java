package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayPolicy;
import clyvasync.Clyvasync.repository.homestay.HomestayPolicyRepository;
import clyvasync.Clyvasync.service.homestay.HomestayPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class HomestayPolicyServiceImpl implements HomestayPolicyService {
    private final HomestayPolicyRepository homestayPolicyRepository;
    @Override
    public HomestayPolicy getHomestayPolicyByHomestayId(Long homestayId) {
        return homestayPolicyRepository.findByHomestayId(homestayId).orElse(new HomestayPolicy());
    }
}
