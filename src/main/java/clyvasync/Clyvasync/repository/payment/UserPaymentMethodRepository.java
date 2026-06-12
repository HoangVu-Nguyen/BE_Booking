package clyvasync.Clyvasync.repository.payment;

import clyvasync.Clyvasync.modules.payment.entity.UserPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserPaymentMethodRepository extends JpaRepository<UserPaymentMethod,Long> {
    boolean existsByUserId(Long userId);
    List<UserPaymentMethod> findByUserIdOrderByIsPrimaryDesc(Long userId);
    List<UserPaymentMethod> findByUserIdOrderByIdAsc(Long userId);
    List<UserPaymentMethod> findByUserId(Long userId);
    @Modifying
    @Query("UPDATE UserPaymentMethod u SET u.isPrimary = false WHERE u.userId = :userId")
    void clearPrimaryStatusByUserId(@Param("userId") Long userId);
}
