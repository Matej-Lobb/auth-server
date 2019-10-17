package sk.mlobb.authserver.service.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import sk.mlobb.authserver.model.Role;
import sk.mlobb.authserver.model.exception.NotFoundException;
import sk.mlobb.authserver.model.permission.Access;
import sk.mlobb.authserver.model.permission.Permission;
import sk.mlobb.authserver.model.rest.request.UpdateRoleRequest;

import java.util.Set;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "id", source = "updateRoleWrapper.role.id")
    @Mapping(target = "role", source = "updateRoleWrapper.role.role")
    @Mapping(target = "permissions", source = "updateRoleWrapper", qualifiedByName = "mapPermissions")
    Role mapUpdateRole(UpdateRoleWrapper updateRoleWrapper);

    Access mapAccess(UpdateRoleRequest.Permission.Access access);

    @Named("mapPermissions")
    default Set<Permission> mapPermissions(UpdateRoleWrapper updateRoleWrapper) {
        if (updateRoleWrapper.getRequest().getPermissions() != null && !updateRoleWrapper.getRequest().getPermissions()
                .isEmpty()) {
            Set<Permission> permissions = updateRoleWrapper.getRole().getPermissions();
            for (UpdateRoleRequest.Permission permission : updateRoleWrapper.getRequest().getPermissions()) {
                Permission dbPermission = getPermission(permission.getNameAlias(), permissions);
                permissions.remove(dbPermission);

                dbPermission.setAccess(mapAccess(permission.getAccess()));
                permissions.add(dbPermission);
            }
        }
        return updateRoleWrapper.getRole().getPermissions();
    }



    private Permission getPermission(String nameAlias, Set<Permission> permissions) {
        Permission finalPermission = null;
        for (Permission permission : permissions) {
            if (permission.getNameAlias().equals(nameAlias)) {
                finalPermission = permission;
            }
        }
        if (finalPermission == null) {
            throw new NotFoundException(String.format("Permission with alias name: %s not Found !", nameAlias));
        }
        return finalPermission;
    }
}
