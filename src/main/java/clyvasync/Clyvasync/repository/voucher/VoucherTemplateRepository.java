package clyvasync.Clyvasync.repository.voucher;

import clyvasync.Clyvasync.modules.voucher.entity.UserVoucher;
import clyvasync.Clyvasync.modules.voucher.entity.VoucherTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

import java.util.Optional;

public interface VoucherTemplateRepository extends JpaRepository<VoucherTemplate, Long> {
    Optional<VoucherTemplate> findByCode(String code);
    boolean existsByCode(String code);

    @Query("SELECT DISTINCT v FROM VoucherTemplate v " +
           "JOIN HostVoucherApplyScope hvas ON v.id = hvas.voucherId " +
           "JOIN Homestay h ON hvas.homestayId = h.id " +
           "WHERE h.ownerId = :ownerId AND v.sponsorType IN ('HOST', 'HOST_SPONSORED') " +
           "ORDER BY v.createdAt DESC")
    List<VoucherTemplate> findHostVouchers(@Param("ownerId") Long ownerId);
}
