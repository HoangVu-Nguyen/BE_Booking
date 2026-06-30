package clyvasync.Clyvasync.repository.voucher;

import clyvasync.Clyvasync.modules.voucher.entity.UserVoucher;
import clyvasync.Clyvasync.modules.voucher.entity.VoucherTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VoucherTemplateRepository extends JpaRepository<VoucherTemplate, Long> {
    Optional<VoucherTemplate> findByCode(String code);
    boolean existsByCode(String code);
}
