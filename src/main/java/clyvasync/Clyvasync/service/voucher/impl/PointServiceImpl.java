package clyvasync.Clyvasync.service.voucher.impl;

import clyvasync.Clyvasync.enums.offer.PointTransactionType;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.auth.entity.User;
import clyvasync.Clyvasync.modules.voucher.entity.UserPointHistory;
import clyvasync.Clyvasync.repository.auth.UserRepository;
import clyvasync.Clyvasync.repository.voucher.UserPointHistoryRepository;
import clyvasync.Clyvasync.service.voucher.PointService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointServiceImpl implements PointService {

    private final UserRepository userRepository;
    private final UserPointHistoryRepository userPointHistoryRepository;

    @Override
    @Transactional
    public void addPointsFromBooking(Long userId, Integer points, Long bookingId, String description) {
        if (points == null || points <= 0) return;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ResultCode.USER_NOT_FOUND));

        user.setRewardPoints(user.getRewardPoints() + points);
        userRepository.save(user);

        UserPointHistory history = UserPointHistory.builder()
                .userId(userId)
                .points(points)
                .transactionType(PointTransactionType.EARN)
                .referenceId(bookingId)
                .description(description)
                .build();
        userPointHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void deductPointsForVoucher(Long userId, Integer points, String description) {
        if (points == null || points <= 0) return;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ResultCode.USER_NOT_FOUND));

        if (user.getRewardPoints() < points) {
            throw new AppException(ResultCode.INSUFFICIENT_FUNDS);
        }

        user.setRewardPoints(user.getRewardPoints() - points);
        userRepository.save(user);

        UserPointHistory history = UserPointHistory.builder()
                .userId(userId)
                .points(-points)
                .transactionType(PointTransactionType.REDEEM)
                .description(description)
                .build();
        userPointHistoryRepository.save(history);
    }

    @Override
    @Transactional
    public void deductPointsForBookingCancellation(Long userId, Integer points, Long bookingId, String description) {
        if (points == null || points <= 0) return;

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new AppException(ResultCode.USER_NOT_FOUND));

        if (user.getRewardPoints() < points) {
            // Even if they don't have enough points, we can let it go negative or just set to 0. 
            // Standard approach is to subtract anyway, or throw. For now, subtract.
        }

        user.setRewardPoints(user.getRewardPoints() - points);
        userRepository.save(user);

        UserPointHistory history = UserPointHistory.builder()
                .userId(userId)
                .points(-points)
                .transactionType(PointTransactionType.REFUND)
                .referenceId(bookingId)
                .description(description)
                .build();
        userPointHistoryRepository.save(history);
    }
}
