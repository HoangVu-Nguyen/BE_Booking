package clyvasync.Clyvasync.service.auth.impl;

import clyvasync.Clyvasync.enums.auth.RoleName;
import clyvasync.Clyvasync.exception.AppException;
import clyvasync.Clyvasync.exception.ResultCode;
import clyvasync.Clyvasync.modules.auth.entity.Role;
import clyvasync.Clyvasync.repository.auth.RoleRepository;
import clyvasync.Clyvasync.service.auth.RoleService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {

    private final RoleRepository roleRepository;

    @Override
    public Role getRoleById(int id) {
        return roleRepository.findRoleById(id)
                .orElseThrow(() -> new AppException(ResultCode.ROLE_NOT_FOUND));
    }

    @Override
    public Role getRoleByName(RoleName roleName) {
        return roleRepository.findByName(roleName)
                .orElseThrow(() -> new AppException(ResultCode.ROLE_NOT_FOUND));
    }

    @Override
    public boolean existsByName(RoleName roleName) {
        return roleRepository.existsByName(roleName);
    }

    @Override
    @Transactional
    public Role saveRole(Role role) {
        log.info("Saving new role to database...");
        return roleRepository.save(role);
    }
}
