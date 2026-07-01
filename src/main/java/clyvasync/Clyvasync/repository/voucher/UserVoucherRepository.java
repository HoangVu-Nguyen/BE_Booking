package clyvasync.Clyvasync.repository.voucher;

import clyvasync.Clyvasync.modules.voucher.entity.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {
    List<UserVoucher> findByUserId(Long userId);
    Optional<UserVoucher> findByIdAndUserId(Long id, Long userId);
}
