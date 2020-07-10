package sk.mlobb.authserver.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sk.mlobb.authserver.db.ApplicationsRepository;
import sk.mlobb.authserver.db.RolesRepository;
import sk.mlobb.authserver.db.UsersRepository;
import sk.mlobb.authserver.model.ApplicationEntity;
import sk.mlobb.authserver.model.RoleEntity;
import sk.mlobb.authserver.model.UserEntity;
import sk.mlobb.authserver.model.exception.ConflictException;
import sk.mlobb.authserver.model.exception.NoContentException;
import sk.mlobb.authserver.model.exception.NotFoundException;
import sk.mlobb.authserver.model.rest.User;
import sk.mlobb.authserver.model.rest.request.CreateUserRequest;
import sk.mlobb.authserver.model.rest.request.UpdateUserRequest;
import sk.mlobb.authserver.service.mappers.UpdateUserWrapper;
import sk.mlobb.authserver.service.mappers.UserMapper;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class UserService {

    private static final String APPLICATION_NOT_EXISTS = "Application not exists !";
    private static final String USER_NOT_FOUND = "User not found";

    private final ApplicationsRepository applicationsRepository;
    private final ApplicationService applicationService;
    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Autowired
    public UserService(ApplicationsRepository applicationsRepository, UsersRepository usersRepository,
                       PasswordEncoder passwordEncoder, UserMapper userMapper, ApplicationService applicationService,
                       RolesRepository rolesRepository) {
        this.applicationsRepository = applicationsRepository;
        this.applicationService = applicationService;
        this.rolesRepository = rolesRepository;
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Transactional
    public List<User> getAllUsers(String applicationUid) {
        log.debug("Getting all users from database.");
        final ApplicationEntity applicationEntity = checkIfApplicationExists(applicationUid);
        final List<User> users = userMapper.mapAllUsers(applicationEntity.getUserEntities());
        if (users.isEmpty()) {
            throw new NoContentException();
        }
        return users;
    }

    @Transactional
    public User getUserByName(String applicationUid, String identifier) {
        log.debug("Get user by identifier: {}", identifier);

        checkIfUserIsPartOfApplication(checkIfApplicationExists(applicationUid), identifier);

        UserEntity userEntity;
        if (isValidEmailAddress(identifier)) {
            userEntity = getUser(usersRepository.findByEmailIgnoreCase(identifier));
        } else {
            userEntity = getUser(usersRepository.findByUsernameIgnoreCase(identifier));
        }
        return userMapper.mapUser(userEntity);
    }

    @Transactional
    public User createUser(String applicationUid, CreateUserRequest createUserRequest) {
        log.debug("Creating user: {}", createUserRequest.getUsername());

        validateUserData(createUserRequest.getUsername(), createUserRequest.getEmail());
        final ApplicationEntity applicationEntity = checkIfApplicationExists(applicationUid);

        final UserEntity userEntity = createUser(userMapper.mapCreateUser(createUserRequest), applicationEntity);
        return userMapper.mapUser(userEntity);
    }

    @Transactional
    public User updateUserByUsername(String applicationUid, String existingUsername,
                                     UpdateUserRequest updateUserRequest, boolean canChangeRole) {
        log.debug("Updating user with username: {} ", existingUsername);

        checkIfUserIsPartOfApplication(checkIfApplicationExists(applicationUid), existingUsername);

        final UserEntity oldUserEntity = usersRepository.findByUsernameIgnoreCase(existingUsername);
        validateIfObjectExists(oldUserEntity == null, USER_NOT_FOUND);

        checkPasswordChange(updateUserRequest, oldUserEntity);
        if (canChangeRole) {
            checkRolesChange(updateUserRequest, oldUserEntity);
        }
        final UserEntity updateUser = usersRepository.save(userMapper.mapUpdateUser(UpdateUserWrapper.builder()
                .userEntity(oldUserEntity).request(updateUserRequest).build()));
        return userMapper.mapUser(updateUser);
    }

    @Transactional
    public void deleteUserByUsername(String applicationUid, String username) {
        log.debug("Deleting User with username {}", username);

        final ApplicationEntity applicationEntity = checkIfApplicationExists(applicationUid);
        checkIfUserIsPartOfApplication(applicationEntity, username);
        final UserEntity userEntity = usersRepository.findByUsernameIgnoreCase(username);
        validateIfObjectExists(userEntity == null, USER_NOT_FOUND);

        userEntity.getRoles().clear();
        usersRepository.saveAndFlush(userEntity);

        applicationEntity.getUserEntities().remove(findUserInApplication(applicationEntity.getUserEntities(),
                userEntity.getUsername()));
        applicationsRepository.saveAndFlush(applicationEntity);

        usersRepository.delete(userEntity);
    }

    @Transactional
    public UserEntity getUserByName(String identifier) {
        log.debug("Get user by identifier: {}", identifier);
        if (isValidEmailAddress(identifier)) {
            return getUser(usersRepository.findByEmailIgnoreCase(identifier));
        } else {
            return getUser(usersRepository.findByUsernameIgnoreCase(identifier));
        }
    }

    private UserEntity createUser(UserEntity userEntity, ApplicationEntity applicationEntity) {
        userEntity.setPassword(passwordEncoder.encode(userEntity.getPassword()));
        final Set<RoleEntity> roleEntities = new HashSet<>();
        roleEntities.add(applicationEntity.getDefaultUserRoleEntity());
        userEntity.setRoles(roleEntities);

        applicationEntity.getUserEntities().add(userEntity);
        return usersRepository.saveAndFlush(findUserInApplication(applicationsRepository.save(applicationEntity)
                        .getUserEntities(), userEntity.getUsername()));
    }

    private UserEntity findUserInApplication(Set<UserEntity> userEntities, String username) {
        for (UserEntity userEntity : userEntities) {
            if (userEntity.getUsername().equals(username)){
                return userEntity;
            }
        }
        throw new NotFoundException("User not found in application !");
    }

    private void checkRolesChange(UpdateUserRequest updateUserRequest, UserEntity oldUserEntity) {
        if (updateUserRequest.getRoles() != null && !updateUserRequest.getRoles().isEmpty()) {
            final Set<RoleEntity> finalRoleEntities = new HashSet<>();
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
        boolean isInCorrectApplication = false;
        for (UserEntity userEntity : applicationEntity.getUserEntities()) {
            if (userEntity.getId().equals(getUserByName(identifier).getId())) {
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
        final boolean isUsername = usersRepository.findByUsernameIgnoreCase(requestUsername) != null;
        if (isUsername) {
            throw new ConflictException(String.format("A user with name %s already exist", requestUsername));
        }
        boolean isEmail = usersRepository.findByEmailIgnoreCase(requestEmail) != null;
        if (isEmail) {
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
