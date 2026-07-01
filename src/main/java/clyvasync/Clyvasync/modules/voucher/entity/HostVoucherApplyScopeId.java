package clyvasync.Clyvasync.modules.voucher.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class HostVoucherApplyScopeId implements Serializable {
    private Long voucherId;
    private Long homestayId;
}
