package sk.mlobb.authserver.service.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import sk.mlobb.authserver.model.ApplicationEntity;
import sk.mlobb.authserver.model.RoleEntity;
import sk.mlobb.authserver.model.UserEntity;
import sk.mlobb.authserver.model.rest.response.Application;
import sk.mlobb.authserver.model.rest.response.Role;
import sk.mlobb.authserver.model.rest.response.User;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface ApplicationMapper {

    @Mapping(target = "defaultUserRole", source = "defaultUserRoleEntity")
    @Mapping(target = "users", source = "userEntities")
    @Mapping(target = "applicationRoles", source = "applicationRoleEntities")
    @Mapping(target = "serviceUsers", source = "serviceUserEntities")
    Application mapApplication(ApplicationEntity applicationEntity);

    User mapUser(UserEntity userEntity);

    @Mapping(source = "role", target = "roleName")
    Role mapRole(RoleEntity roleEntity);
}
