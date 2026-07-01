package clyvasync.Clyvasync.repository.voucher;

import clyvasync.Clyvasync.modules.voucher.entity.HostVoucherApplyScope;
import clyvasync.Clyvasync.modules.voucher.entity.HostVoucherApplyScopeId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HostVoucherApplyScopeRepository extends JpaRepository<HostVoucherApplyScope, HostVoucherApplyScopeId> {
    List<HostVoucherApplyScope> findByVoucherId(Long voucherId);
    void deleteByVoucherId(Long voucherId);
}
