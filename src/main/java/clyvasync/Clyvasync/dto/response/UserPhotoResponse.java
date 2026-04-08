package clyvasync.Clyvasync.dto.response;

import clyvasync.Clyvasync.enums.media.ImageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
@Data
@AllArgsConstructor
public class UserPhotoResponse {
    private Long id;

    private Long userId;

    private String photoUrl;


    private ImageType photoType;

    private Boolean isCurrent;

    private LocalDateTime createdAt;
}
