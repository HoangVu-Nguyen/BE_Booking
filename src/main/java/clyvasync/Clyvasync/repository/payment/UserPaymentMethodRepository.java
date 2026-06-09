package clyvasync.Clyvasync.repository.payment;

import clyvasync.Clyvasync.modules.payment.entity.UserPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserPaymentMethodRepository extends JpaRepository<UserPaymentMethod,Long> {
    boolean existsByUserId(Long userId);
    List<UserPaymentMethod> findByUserIdOrderByIsPrimaryDesc(Long userId);
}
