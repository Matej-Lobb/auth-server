package sk.mlobb.authserver.service.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sk.mlobb.authserver.model.Application;
import sk.mlobb.authserver.model.User;
import sk.mlobb.authserver.model.rest.CreateUserRequest;
import sk.mlobb.authserver.model.rest.UpdateUserRequest;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "license", ignore = true)
    @Mapping(target = "application", source = "application")
    User mapCreateUser(CreateUserRequest createUserRequest, Application application);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "username", ignore = true)
    @Mapping(target = "application", source = "application")
    User mapUpdateUser(UpdateUserRequest updateUserRequest, Application application);
}
