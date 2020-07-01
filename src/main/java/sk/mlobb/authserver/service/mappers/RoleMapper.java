package sk.mlobb.authserver.service.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValueCheckStrategy;
import sk.mlobb.authserver.model.RoleEntity;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface RoleMapper {

    @Mapping(target = "id", source = "updateRoleWrapper.roleEntity.id")
    @Mapping(target = "role", source = "updateRoleWrapper.roleEntity.role")
    RoleEntity mapUpdateRole(UpdateRoleWrapper updateRoleWrapper);
}
