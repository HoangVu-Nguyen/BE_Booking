package clyvasync.Clyvasync.repository.voucher;

import clyvasync.Clyvasync.modules.voucher.entity.UserVoucher;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface UserVoucherRepository extends JpaRepository<UserVoucher, Long> {
    List<UserVoucher> findByUserId(Long userId);
}
