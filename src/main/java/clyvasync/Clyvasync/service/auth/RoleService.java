package clyvasync.Clyvasync.service.auth;

import clyvasync.Clyvasync.enums.auth.RoleName;
import clyvasync.Clyvasync.modules.auth.entity.Role;
import org.springframework.stereotype.Service;

@Service
public interface RoleService {
    Role getRoleById(int id);

    Role getRoleByName(RoleName roleName);

    boolean existsByName(RoleName roleName);

    Role saveRole(Role role);

}
