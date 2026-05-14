package clyvasync.Clyvasync.dto.detail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public  class PolicyDetail {
    private java.time.LocalTime checkInTime;
    private java.time.LocalTime checkOutTime;
    private String lateCheckInInstruction;
    private Boolean allowsPets;
    private Boolean allowsSmoking;
    private Boolean allowsParties;
}