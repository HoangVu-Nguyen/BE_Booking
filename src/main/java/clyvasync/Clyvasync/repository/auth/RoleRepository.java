package clyvasync.Clyvasync.repository.auth;

import clyvasync.Clyvasync.enums.auth.RoleName;
import clyvasync.Clyvasync.modules.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role,Long> {
    Optional<Role> findRoleById(int id);
    Optional<Role> findByName(RoleName roleName);
    boolean existsByName(RoleName roleName);

}
