package clyvasync.Clyvasync.dto.response;


import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookingPolicyResponse {
    private String checkInTime;
    private String checkOutTime;
    private Boolean allowsPets;
    private Boolean allowsSmoking;
    private Boolean allowsParties;
}