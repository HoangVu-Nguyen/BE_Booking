package clyvasync.Clyvasync.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")

    private LocalDate issueDate;
    private String issueBy;

    private String frontImage;
    private String backImage;
    private String selfie;

    private Double aiScore;
    private String ocrData;
}