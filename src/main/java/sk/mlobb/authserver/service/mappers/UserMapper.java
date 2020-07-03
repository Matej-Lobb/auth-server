package sk.mlobb.authserver.service.mappers;

import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.NullValueCheckStrategy;
import sk.mlobb.authserver.model.UserEntity;
import sk.mlobb.authserver.model.rest.request.CreateUserRequest;
import sk.mlobb.authserver.model.rest.User;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Mapper(nullValueCheckStrategy = NullValueCheckStrategy.ALWAYS, componentModel = "spring")
public interface UserMapper {

    default List<User> mapAllUsers(Set<UserEntity> userEntities) {
        final List<User> users = new ArrayList<>();
        for (UserEntity userEntity: userEntities) {
            users.add(mapUser(userEntity));
        }
        return users;
    }

    User mapUser(UserEntity userEntity);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "roles", ignore = true)
    UserEntity mapCreateUser(CreateUserRequest createUserRequest);

    @Mapping(target = "id", source = "updateUserWrapper.userEntity.id")
    @Mapping(target = "roles", source = "updateUserWrapper.userEntity.roles")
    @Mapping(target = "username", source = "updateUserWrapper.userEntity.username")
    @Mapping(target = "password", source = "updateUserWrapper.userEntity.password")
    @Mapping(target = "active", source = "updateUserWrapper", qualifiedByName = "mapActive")
    @Mapping(target = "keepUpdated", source = "updateUserWrapper", qualifiedByName = "mapKeepUpdated")
    @Mapping(target = "profilePicture", source = "updateUserWrapper", qualifiedByName = "mapProfilePicture")
    @Mapping(target = "dateOfBirth", source = "updateUserWrapper", qualifiedByName = "mapDob")
    @Mapping(target = "email", source = "updateUserWrapper", qualifiedByName = "mapEmail")
    @Mapping(target = "firstName", source = "updateUserWrapper", qualifiedByName = "mapFirstName")
    @Mapping(target = "lastName", source = "updateUserWrapper", qualifiedByName = "mapLastName")
    @Mapping(target = "country", source = "updateUserWrapper", qualifiedByName = "mapCountry")
    UserEntity mapUpdateUser(UpdateUserWrapper updateUserWrapper);

    @Named("mapFirstName")
    default String mapFirstName(UpdateUserWrapper updateUserWrapper) {
        if (StringUtils.isEmpty(updateUserWrapper.getRequest().getFirstName())) {
            return updateUserWrapper.getUserEntity().getFirstName();
        }
        return updateUserWrapper.getRequest().getFirstName();
    }

    @Named("mapLastName")
    default String mapLastName(UpdateUserWrapper updateUserWrapper) {
        if (StringUtils.isEmpty(updateUserWrapper.getRequest().getLastName())) {
            return updateUserWrapper.getUserEntity().getLastName();
        }
        return updateUserWrapper.getRequest().getLastName();
    }

    @Named("mapCountry")
    default String mapCountry(UpdateUserWrapper updateUserWrapper) {
        if (StringUtils.isEmpty(updateUserWrapper.getRequest().getCountry())) {
            return updateUserWrapper.getUserEntity().getCountry();
        }
        return updateUserWrapper.getRequest().getCountry();
    }

    @Named("mapEmail")
    default String mapEmail(UpdateUserWrapper updateUserWrapper) {
        if (StringUtils.isEmpty(updateUserWrapper.getRequest().getEmail())) {
            return updateUserWrapper.getUserEntity().getEmail();
        }
        return updateUserWrapper.getRequest().getEmail();
    }

    @Named("mapDob")
    default LocalDate mapDob(UpdateUserWrapper updateUserWrapper) {
        if (updateUserWrapper.getRequest().getDateOfBirth() == null) {
            return updateUserWrapper.getUserEntity().getDateOfBirth();
        }
        return updateUserWrapper.getRequest().getDateOfBirth();
    }

    @Named("mapProfilePicture")
    default byte[] mapProfilePicture(UpdateUserWrapper updateUserWrapper) {
        if (updateUserWrapper.getRequest().getProfilePicture() == null) {
            return updateUserWrapper.getUserEntity().getProfilePicture();
        }
        return updateUserWrapper.getRequest().getProfilePicture();
    }

    @Named("mapActive")
    default Boolean mapActive(UpdateUserWrapper updateUserWrapper) {
        if (updateUserWrapper.getRequest().getActive() == null) {
            return updateUserWrapper.getUserEntity().getActive();
        }
        return updateUserWrapper.getRequest().getActive();
    }

    @Named("mapKeepUpdated")
    default Boolean mapKeepUpdated(UpdateUserWrapper updateUserWrapper) {
        if (updateUserWrapper.getRequest().getKeepUpdated() == null) {
            return updateUserWrapper.getUserEntity().getKeepUpdated();
        }
        return updateUserWrapper.getRequest().getKeepUpdated();
    }
}
