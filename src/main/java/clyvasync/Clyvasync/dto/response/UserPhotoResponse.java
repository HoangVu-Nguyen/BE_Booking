package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.enums.media.ImageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserPhotoResponse implements Serializable {
    private Long id;

    private Long userId;

    private String photoUrl;

    private ImageType photoType;

    private Boolean isCurrent;

    private LocalDateTime createdAt;


}
