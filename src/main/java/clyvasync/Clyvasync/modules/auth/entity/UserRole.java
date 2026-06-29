package clyvasync.Clyvasync.modules.auth.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_roles")
public class UserRole {
    @Id
    private Long userId;
    private Integer roleId;
}