package sk.mlobb.authserver.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sk.mlobb.authserver.db.RolesRepository;
import sk.mlobb.authserver.db.UsersRepository;
import sk.mlobb.authserver.model.ApplicationEntity;
import sk.mlobb.authserver.model.RoleEntity;
import sk.mlobb.authserver.model.UserEntity;
import sk.mlobb.authserver.model.exception.ConflictException;
import sk.mlobb.authserver.model.exception.NotFoundException;
import sk.mlobb.authserver.model.rest.request.CreateUserRequest;
import sk.mlobb.authserver.model.rest.request.UpdateUserRequest;
import sk.mlobb.authserver.model.rest.User;
import sk.mlobb.authserver.service.mappers.UpdateUserWrapper;
import sk.mlobb.authserver.service.mappers.UserMapper;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Transactional
public class UserService {

    private static final String APPLICATION_NOT_EXISTS = "Application not exists !";
    private static final String USER_NOT_FOUND = "User not found";

    private final ApplicationService applicationService;
    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UsersRepository usersRepository, PasswordEncoder passwordEncoder, UserMapper userMapper,
                       ApplicationService applicationService, RolesRepository rolesRepository) {
        this.applicationService = applicationService;
        this.rolesRepository = rolesRepository;
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public List<User> getAllUsers(String applicationUid) {
        log.debug("Getting all users from database.");
        ApplicationEntity applicationEntity = checkIfApplicationExists(applicationUid);
        return userMapper.mapAllUsers(applicationEntity.getUserEntities());
    }

    public User getUserByName(String applicationUid, String identifier) {
        log.debug("Get user by identifier: {}", identifier);

        checkIfUserIsPartOfApplication(applicationUid, identifier);

        UserEntity userEntity;
        if (isValidEmailAddress(identifier)) {
            userEntity = getUser(usersRepository.findByEmailIgnoreCase(identifier));
        } else {
            userEntity = getUser(usersRepository.findByUsernameIgnoreCase(identifier));
        }
        return userMapper.mapUser(userEntity);
    }

    public User createUser(String applicationUid, CreateUserRequest createUserRequest) {
        log.debug("Creating user: {}", createUserRequest.getUsername());

        validateUserData(createUserRequest.getUsername(), createUserRequest.getEmail());
        ApplicationEntity applicationEntity = checkIfApplicationExists(applicationUid);

        UserEntity userEntity = createUser(userMapper.mapCreateUser(createUserRequest), applicationEntity);
        return userMapper.mapUser(userEntity);
    }

    public User updateUserByUsername(String applicationUid, String existingUsername,
                                     UpdateUserRequest updateUserRequest, boolean canChangeRole) {
        log.debug("Updating user with username: {} ", existingUsername);

        checkIfUserIsPartOfApplication(applicationUid, existingUsername);

        final UserEntity oldUserEntity = usersRepository.findByUsernameIgnoreCase(existingUsername);
        validateIfObjectExists(oldUserEntity == null, USER_NOT_FOUND);

        checkPasswordChange(updateUserRequest, oldUserEntity);
        if (canChangeRole) {
            checkRolesChange(updateUserRequest, oldUserEntity);
        }
        UserEntity updatedUserEntity = userMapper.mapUpdateUser(UpdateUserWrapper.builder().userEntity(oldUserEntity)
                .request(updateUserRequest).build());
        UserEntity updateUser = usersRepository.save(updatedUserEntity);
        return userMapper.mapUser(updateUser);
    }

    public void deleteUserByUsername(String applicationUid, String username) {
        log.debug("Deleting User with username {}", username);

        checkIfUserIsPartOfApplication(applicationUid, username);

        UserEntity userEntity = usersRepository.findByUsernameIgnoreCase(username);
        validateIfObjectExists(userEntity == null, USER_NOT_FOUND);

        usersRepository.deleteById(userEntity.getId());
    }

    public UserEntity getUserByName(String identifier) {
        log.debug("Get user by identifier: {}", identifier);
        if (isValidEmailAddress(identifier)) {
            return getUser(usersRepository.findByEmailIgnoreCase(identifier));
        } else {
            return getUser(usersRepository.findByUsernameIgnoreCase(identifier));
        }
    }

    public void checkIfUserIsPartOfApplication(String uid, String identifier) {
        checkIfUserIsPartOfApplication(checkIfApplicationExists(uid), identifier);
    }

    private UserEntity createUser(UserEntity userEntity, ApplicationEntity applicationEntity) {
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        Set<RoleEntity> roleEntities = new HashSet<>();
        roleEntities.add(applicationEntity.getDefaultUserRoleEntity());
        userEntity.setId(0L);
        userEntity.setRoles(roleEntities);
        userEntity.setApplicationEntity(applicationEntity);
        return usersRepository.save(userEntity);
    }

    private void checkRolesChange(UpdateUserRequest updateUserRequest, UserEntity oldUserEntity) {
        if (updateUserRequest.getRoles() != null && !updateUserRequest.getRoles().isEmpty()) {
            Set<RoleEntity> finalRoleEntities = new HashSet<>();
            for (String role : updateUserRequest.getRoles()) {
                RoleEntity dbRoleEntity = rolesRepository.findByRole(role);
                validateIfObjectExists(dbRoleEntity == null, String.format("Role: %s not found !", role));
                finalRoleEntities.add(dbRoleEntity);
            }
            oldUserEntity.setRoles(finalRoleEntities);
        }
    }

    private void checkPasswordChange(UpdateUserRequest updateUserRequest, UserEntity oldUserEntity) {
        if (!StringUtils.isEmpty(updateUserRequest.getPassword()) && !oldUserEntity.getPassword()
                .equals(updateUserRequest.getPassword())) {
            oldUserEntity.setPassword(passwordEncoder.encode(updateUserRequest.getPassword()));
        }
    }

    private void validateIfObjectExists(boolean exists, String message) {
        if (exists) {
            throw new NotFoundException(message);
        }
    }

    private ApplicationEntity checkIfApplicationExists(String applicationUid) {
        final ApplicationEntity applicationEntity = applicationService.getApplicationByUid(applicationUid);
        validateIfObjectExists(applicationEntity == null, APPLICATION_NOT_EXISTS);
        return applicationEntity;
    }

    private void checkIfUserIsPartOfApplication(ApplicationEntity applicationEntity, String identifier) {
        Set<UserEntity> userEntities = applicationEntity.getUserEntities();
        UserEntity dbUserEntity = getUserByName(identifier);

        boolean isInCorrectApplication = false;
        for (UserEntity userEntity : userEntities) {
            if (userEntity.getId().equals(dbUserEntity.getId())) {
                isInCorrectApplication = true;
                break;
            }
        }
        if (! isInCorrectApplication) {
            throw new NotFoundException("User not found in current application !");
        }
    }

    private UserEntity getUser(UserEntity userEntity) {
        validateIfObjectExists(userEntity == null, USER_NOT_FOUND);
        return userEntity;
    }

    private void validateUserData(String requestUsername, String requestEmail) {
        boolean username = usersRepository.findByUsernameIgnoreCase(requestUsername) != null;
        if (username) {
            throw new ConflictException(String.format("A user with name %s already exist", requestUsername));
        }
        boolean email = usersRepository.findByEmailIgnoreCase(requestEmail) != null;
        if (email) {
            throw new ConflictException(String.format("A user with email %s already exist", requestEmail));
        }
    }

    private boolean isValidEmailAddress(String email) {
        boolean result = true;
        try {
            final InternetAddress emailAddress = new InternetAddress(email);
            emailAddress.validate();
        } catch (AddressException e) {
            result = false;
        }
        return result;
    }
}
