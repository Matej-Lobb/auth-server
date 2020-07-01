package sk.mlobb.authserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sk.mlobb.authserver.db.RolesRepository;
import sk.mlobb.authserver.model.RoleEntity;
import sk.mlobb.authserver.model.exception.NotFoundException;
import sk.mlobb.authserver.model.rest.request.UpdateRoleRequest;
import sk.mlobb.authserver.service.mappers.RoleMapper;
import sk.mlobb.authserver.service.mappers.UpdateRoleWrapper;

@Slf4j
@Service
public class RoleService {

    private final RolesRepository rolesRepository;
    private final RoleMapper roleMapper;

    public RoleService(RolesRepository rolesRepository, RoleMapper roleMapper) {
        this.rolesRepository = rolesRepository;
        this.roleMapper = roleMapper;
    }

    @Transactional
    public RoleEntity addRole(String roleName) {
        log.debug("Adding role: {}", roleName);
        return rolesRepository.save(RoleEntity.builder().role(roleName).build());
    }

    @Transactional
    public RoleEntity getRoleByName(String role) {
        log.debug("Getting role: {}", role);
        RoleEntity dbRoleEntity = rolesRepository.findByRole(role);
        if (dbRoleEntity == null) {
            throw new NotFoundException("Role not found !");
        }
        return dbRoleEntity;
    }

    @Transactional
    public void updateRole(String role, UpdateRoleRequest updateRoleRequest) {
        log.debug("Updating role: {}", role);
        RoleEntity dbRoleEntity = roleMapper.mapUpdateRole(UpdateRoleWrapper.builder().roleEntity(getRoleByName(role))
                .request(updateRoleRequest).build());
        rolesRepository.save(dbRoleEntity);
    }

    @Transactional
    public void deleteRole(String roleName) {
        log.debug("Deleting role: {}", roleName);
        RoleEntity roleEntity = getRoleByName(roleName);
        rolesRepository.delete(roleEntity);
    }
}
