package clyvasync.Clyvasync.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateConversationRequest {
    @NotNull(message = "Reference ID (Tour/Booking ID) không được để trống")
    private Long referenceId;

    @NotBlank(message = "Tên nhóm không được để trống")
    private String name;

    @NotEmpty(message = "Danh sách thành viên không được rỗng")
    private List<Long> participantIds;
}
