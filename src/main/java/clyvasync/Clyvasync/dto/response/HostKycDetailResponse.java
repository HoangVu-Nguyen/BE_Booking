package clyvasync.Clyvasync.dto.response;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class HostKycDetailResponse {
    private Long profileId;
    private String name;
    private String email;
    private String phone;

    private String citizenId;
    private LocalDate issueDate;
    private String issueBy;

    private String frontImage;
    private String backImage;
    private String selfie;

    private Double aiScore;
    private String ocrData;
}