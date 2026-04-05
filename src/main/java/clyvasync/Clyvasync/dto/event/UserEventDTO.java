package clyvasync.Clyvasync.dto.event;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserEventDTO {
    private String email;
    private String fullName;
    private String code;
}
