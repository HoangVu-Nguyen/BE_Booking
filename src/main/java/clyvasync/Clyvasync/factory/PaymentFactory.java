package clyvasync.Clyvasync.factory;

import clyvasync.Clyvasync.enums.payment.PaymentMethod;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.strategy.PaymentStrategy;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class PaymentFactory {
    private final Map<PaymentMethod, PaymentStrategy> strategies;

    // Spring tự động inject tất cả beans kiểu PaymentStrategy vào đây
    public PaymentFactory(List<PaymentStrategy> strategyList) {
        strategies = strategyList.stream()
                .collect(Collectors.toMap(PaymentStrategy::getMethod, s -> s));
    }

    public PaymentStrategy getStrategy(PaymentMethod method) {
        PaymentStrategy strategy = strategies.get(method);
        if (strategy == null) throw new AppException(ResultCode.PAYMENT_METHOD_NOT_SUPPORTED);
        return strategy;
    }
}