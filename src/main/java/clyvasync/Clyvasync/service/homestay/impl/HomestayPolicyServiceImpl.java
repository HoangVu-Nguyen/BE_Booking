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

import java.math.BigDecimal;
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
    @Transactional
    public RoomPolicyResponse updatePolicy(
            Long ownerId,
            Long homestayId,
            UpdateHomestayPolicyRequest request
    ) {
        validateHomestayOwner(ownerId, homestayId);

        HomestayPolicy policy = homestayPolicyRepository.findByHomestayId(homestayId)
                .orElseGet(() -> createDefaultPolicy(homestayId));

        applyRequest(policy, request);

        HomestayPolicy saved = homestayPolicyRepository.save(policy);

        return homestayPolicyMapper.toRoomPolicyResponse(saved);
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
    private void applyRequest(HomestayPolicy policy, UpdateHomestayPolicyRequest request) {
        if (request.getCheckInFrom() != null) {
            policy.setCheckInTime(request.getCheckInFrom());
        }

        if (request.getCheckInTo() != null) {
            policy.setCheckInUntil(request.getCheckInTo());
        }

        if (request.getCheckOutBefore() != null) {
            policy.setCheckOutTime(request.getCheckOutBefore());
        }

        if (request.getMinNights() != null) {
            policy.setMinNights(request.getMinNights());
        }

        if (request.getMaxNights() != null) {
            policy.setMaxNights(request.getMaxNights());
        }

        if (request.getBookingMode() != null) {
            policy.setBookingMode(request.getBookingMode());
        }

        if (request.getCancellationPolicy() != null) {
            policy.setCancellationPolicy(request.getCancellationPolicy());
        }

        if (request.getChildrenAllowed() != null) {
            policy.setAllowsChildren(request.getChildrenAllowed());
        }

        if (request.getPetsAllowed() != null) {
            policy.setAllowsPets(request.getPetsAllowed());
        }

        if (request.getSmokingAllowed() != null) {
            policy.setAllowsSmoking(request.getSmokingAllowed());
        }

        if (request.getPartyAllowed() != null) {
            policy.setAllowsParties(request.getPartyAllowed());
        }

        if (request.getQuietHoursEnabled() != null) {
            policy.setQuietHoursEnabled(request.getQuietHoursEnabled());
        }

        if (request.getQuietFrom() != null) {
            policy.setQuietFrom(request.getQuietFrom());
        }

        if (request.getQuietTo() != null) {
            policy.setQuietTo(request.getQuietTo());
        }

        if (request.getDepositRequired() != null) {
            policy.setDepositRequired(request.getDepositRequired());

            if (!request.getDepositRequired()) {
                policy.setDepositAmount(null);
            }
        }

        if (request.getDepositAmount() != null) {
            if (request.getDepositAmount().compareTo(BigDecimal.ZERO) < 0) {
                throw new RuntimeException("Tiền cọc không được âm");
            }

            policy.setDepositAmount(request.getDepositAmount());
        }

        if (request.getLateCheckInInstruction() != null) {
            policy.setLateCheckInInstruction(request.getLateCheckInInstruction());
        }

        if (request.getExtraNotes() != null) {
            policy.setExtraNotes(request.getExtraNotes());
        }

        validatePolicy(policy);
    }
    private void validatePolicy(HomestayPolicy policy) {
        if (policy.getMinNights() != null && policy.getMinNights() < 1) {
            throw new RuntimeException("Số đêm tối thiểu phải lớn hơn hoặc bằng 1");
        }

        if (
                policy.getMaxNights() != null
                        && policy.getMinNights() != null
                        && policy.getMaxNights() < policy.getMinNights()
        ) {
            throw new RuntimeException("Số đêm tối đa không được nhỏ hơn số đêm tối thiểu");
        }

        if (
                Boolean.TRUE.equals(policy.getDepositRequired())
                        && policy.getDepositAmount() == null
        ) {
            throw new RuntimeException("Vui lòng nhập số tiền cọc");
        }
    }
}
