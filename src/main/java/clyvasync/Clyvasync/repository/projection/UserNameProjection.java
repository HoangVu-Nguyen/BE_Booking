package clyvasync.Clyvasync.repository.projection;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface UserNameProjection {
    String getUsername();
    String getEmail();
    LocalDateTime getCreatedAt();
    String getPhoneNumber();
}