package sk.mlobb.authserver.service.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import sk.mlobb.authserver.model.User;
import sk.mlobb.authserver.model.rest.request.CreateUserRequest;
import sk.mlobb.authserver.model.rest.request.UpdateUserRequest;

@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "license", ignore = true)
    User mapCreateUser(CreateUserRequest createUserRequest);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "license", ignore = true)
    @Mapping(target = "username", ignore = true)
    User mapUpdateUser(UpdateUserRequest updateUserRequest);
}
