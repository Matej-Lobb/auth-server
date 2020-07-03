package sk.mlobb.authserver.service.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import sk.mlobb.authserver.model.ApplicationEntity;
import sk.mlobb.authserver.model.RoleEntity;
import sk.mlobb.authserver.model.UserEntity;
import sk.mlobb.authserver.model.rest.Application;
import sk.mlobb.authserver.model.rest.Role;
import sk.mlobb.authserver.model.rest.User;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface ApplicationMapper {

    @Mapping(target = "defaultUserRole", source = "defaultUserRoleEntity")
    @Mapping(target = "users", source = "userEntities")
    @Mapping(target = "applicationRoles", source = "applicationRoleEntities")
    Application mapApplication(ApplicationEntity applicationEntity);

    User mapUser(UserEntity userEntity);

    @Mapping(source = "role", target = "roleName")
    Role mapRole(RoleEntity roleEntity);
}
