package clyvasync.Clyvasync.service.homestay.impl;

import clyvasync.Clyvasync.dto.request.UpdateHomestayPolicyRequest;
import clyvasync.Clyvasync.dto.response.RoomPolicyResponse;
import clyvasync.Clyvasync.enums.booking.BookingMode;
import clyvasync.Clyvasync.enums.room.CancellationPolicy;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.mapper.homestay.HomestayMapper;
import clyvasync.Clyvasync.mapper.homestay.HomestayPolicyMapper;
import clyvasync.Clyvasync.modules.homestay.entity.Homestay;
import clyvasync.Clyvasync.modules.homestay.entity.HomestayPolicy;
import clyvasync.Clyvasync.repository.homestay.HomestayPolicyRepository;
import clyvasync.Clyvasync.repository.homestay.HomestayRepository;
import clyvasync.Clyvasync.service.homestay.HomestayPolicyService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomestayPolicyServiceImpl implements HomestayPolicyService {
    private final HomestayPolicyRepository homestayPolicyRepository;
    private final HomestayPolicyMapper homestayPolicyMapper ;
    private final HomestayRepository homestayRepository;
    @Override
    public HomestayPolicy getHomestayPolicyByHomestayId(Long homestayId) {
        return homestayPolicyRepository.findByHomestayId(homestayId).orElse(new HomestayPolicy());
    }

    @Override
    public List<HomestayPolicy> findAllByHomestayId(Long homestayId) {
        return homestayPolicyRepository.findAllByHomestayId(homestayId);
    }

    @Override
    @Transactional
    public RoomPolicyResponse getPolicy(Long ownerId, Long homestayId) {
        validateHomestayOwner(ownerId, homestayId);

        HomestayPolicy policy = homestayPolicyRepository.findByHomestayId(homestayId)
                .orElseGet(() -> homestayPolicyRepository.save(createDefaultPolicy(homestayId)));

        return homestayPolicyMapper.toRoomPolicyResponse(policy);
    }

    @Override
    public RoomPolicyResponse updatePolicy(Long ownerId, Long homestayId, UpdateHomestayPolicyRequest request) {
        return null;
    }
    private void validateHomestayOwner(Long ownerId, Long homestayId) {
        Homestay homestay = homestayRepository.findById(homestayId)
                .orElseThrow(() -> new AppException(ResultCode.HOMESTAY_NOT_FOUND));

        if (!homestay.getOwnerId().equals(ownerId)) {
            throw new AppException(ResultCode.ACCESS_DENIED);
        }
    }
    private HomestayPolicy createDefaultPolicy(Long homestayId) {
        return HomestayPolicy.builder()
                .homestayId(homestayId)
                .checkInTime(LocalTime.of(14, 0))
                .checkInUntil(LocalTime.of(22, 0))
                .checkOutTime(LocalTime.of(12, 0))
                .minNights(1)
                .maxNights(null)
                .bookingMode(BookingMode.INSTANT_BOOKING)
                .cancellationPolicy(CancellationPolicy.FLEXIBLE)
                .allowsChildren(true)
                .allowsPets(false)
                .allowsSmoking(false)
                .allowsParties(false)
                .quietHoursEnabled(true)
                .quietFrom(LocalTime.of(22, 0))
                .quietTo(LocalTime.of(6, 0))
                .depositRequired(false)
                .depositAmount(null)
                .lateCheckInInstruction(null)
                .extraNotes("Vui lòng giữ gìn vệ sinh chung và không gây ồn sau 22:00.")
                .build();
    }

}
