package clyvasync.Clyvasync.modules.auth.entity;

import clyvasync.Clyvasync.enums.auth.RoleName;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id; // Dùng Integer thay vì int

    @Enumerated(EnumType.STRING)
    @Column(name = "name", unique = true, nullable = false, length = 50) // Giới hạn length
    private RoleName name; // Đổi thành name cho chuẩn Clean Code
}