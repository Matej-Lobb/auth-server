package sk.mlobb.authserver.service.mappers;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import sk.mlobb.authserver.model.User;
import sk.mlobb.authserver.model.rest.request.CreateUserRequest;

import java.time.LocalDate;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface UserMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    @Mapping(target = "license", ignore = true)
    User mapCreateUser(CreateUserRequest createUserRequest);

    @Mapping(target = "id", source = "updateUserWrapper.user.id")
    @Mapping(target = "roles", source = "updateUserWrapper.user.roles")
    @Mapping(target = "license", source = "updateUserWrapper.user.license")
    @Mapping(target = "username", source = "updateUserWrapper.user.username")
    @Mapping(target = "password", source = "updateUserWrapper.user.password")
    @Mapping(target = "active", source = "updateUserWrapper", qualifiedByName = "mapActive")
    @Mapping(target = "keepUpdated", source = "updateUserWrapper", qualifiedByName = "mapKeepUpdated")
    @Mapping(target = "profilePicture", source = "updateUserWrapper", qualifiedByName = "mapProfilePicture")
    @Mapping(target = "dateOfBirth", source = "updateUserWrapper", qualifiedByName = "mapDob")
    @Mapping(target = "email", source = "updateUserWrapper", qualifiedByName = "mapEmail")
    @Mapping(target = "firstName", source = "updateUserWrapper", qualifiedByName = "mapFirstName")
    @Mapping(target = "lastName", source = "updateUserWrapper", qualifiedByName = "mapLastName")
    @Mapping(target = "country", source = "updateUserWrapper", qualifiedByName = "mapCountry")
    User mapUpdateUser(UpdateUserWrapper updateUserWrapper);

    @Named("mapFirstName")
    default String mapFirstName(UpdateUserWrapper updateUserWrapper) {
        if (StringUtils.isEmpty(updateUserWrapper.getRequest().getFirstName())) {
            return updateUserWrapper.getUser().getFirstName();
        }
        return updateUserWrapper.getRequest().getFirstName();
    }

    @Named("mapLastName")
    default String mapLastName(UpdateUserWrapper updateUserWrapper) {
        if (StringUtils.isEmpty(updateUserWrapper.getRequest().getLastName())) {
            return updateUserWrapper.getUser().getLastName();
        }
        return updateUserWrapper.getRequest().getLastName();
    }

    @Named("mapCountry")
    default String mapCountry(UpdateUserWrapper updateUserWrapper) {
        if (StringUtils.isEmpty(updateUserWrapper.getRequest().getCountry())) {
            return updateUserWrapper.getUser().getCountry();
        }
        return updateUserWrapper.getRequest().getCountry();
    }

    @Named("mapEmail")
    default String mapEmail(UpdateUserWrapper updateUserWrapper) {
        if (StringUtils.isEmpty(updateUserWrapper.getRequest().getEmail())) {
            return updateUserWrapper.getUser().getEmail();
        }
        return updateUserWrapper.getRequest().getEmail();
    }

    @Named("mapDob")
    default LocalDate mapDob(UpdateUserWrapper updateUserWrapper) {
        if (updateUserWrapper.getRequest().getDateOfBirth() == null) {
            return updateUserWrapper.getUser().getDateOfBirth();
        }
        return updateUserWrapper.getRequest().getDateOfBirth();
    }

    @Named("mapProfilePicture")
    default byte[] mapProfilePicture(UpdateUserWrapper updateUserWrapper) {
        if (updateUserWrapper.getRequest().getProfilePicture() == null) {
            return updateUserWrapper.getUser().getProfilePicture();
        }
        return updateUserWrapper.getRequest().getProfilePicture();
    }

    @Named("mapActive")
    default Boolean mapActive(UpdateUserWrapper updateUserWrapper) {
        if (updateUserWrapper.getRequest().getActive() == null) {
            return updateUserWrapper.getUser().getActive();
        }
        return updateUserWrapper.getRequest().getActive();
    }

    @Named("mapKeepUpdated")
    default Boolean mapKeepUpdated(UpdateUserWrapper updateUserWrapper) {
        if (updateUserWrapper.getRequest().getKeepUpdated() == null) {
            return updateUserWrapper.getUser().getKeepUpdated();
        }
        return updateUserWrapper.getRequest().getKeepUpdated();
    }
}
