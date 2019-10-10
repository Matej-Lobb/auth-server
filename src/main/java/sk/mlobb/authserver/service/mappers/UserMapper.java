package sk.mlobb.authserver.service.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;
import sk.mlobb.authserver.model.Application;
import sk.mlobb.authserver.model.User;
import sk.mlobb.authserver.model.rest.CreateUserRequest;

@Component
@Mapper(componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "application", source = "application")
    User mapUser(CreateUserRequest createUserRequest, Application application);
}
