package clyvasync.Clyvasync.dto.response;


import java.io.Serializable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserHeaderResponse implements Serializable {
    private Long id;
    private String username;
    private String photoUrl;

    // private String role;
}