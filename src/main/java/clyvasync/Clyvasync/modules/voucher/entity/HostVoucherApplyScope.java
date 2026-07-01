package clyvasync.Clyvasync.modules.voucher.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "host_voucher_apply_scope")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@IdClass(HostVoucherApplyScopeId.class)
public class HostVoucherApplyScope {

    @Id
    @Column(name = "voucher_id")
    private Long voucherId;

    @Id
    @Column(name = "homestay_id")
    private Long homestayId;
}
