package sk.mlobb.authserver.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sk.mlobb.authserver.db.ApplicationUsersRepository;
import sk.mlobb.authserver.db.RolesRepository;
import sk.mlobb.authserver.db.UsersRepository;
import sk.mlobb.authserver.model.Application;
import sk.mlobb.authserver.model.ApplicationUser;
import sk.mlobb.authserver.model.Role;
import sk.mlobb.authserver.model.User;
import sk.mlobb.authserver.model.exception.ConflictException;
import sk.mlobb.authserver.model.exception.NotFoundException;
import sk.mlobb.authserver.model.rest.request.CheckUserExistenceRequest;
import sk.mlobb.authserver.model.rest.request.CreateUserRequest;
import sk.mlobb.authserver.model.rest.request.UpdateUserRequest;
import sk.mlobb.authserver.model.rest.response.CheckUserExistenceResponse;
import sk.mlobb.authserver.service.mappers.UpdateUserWrapper;
import sk.mlobb.authserver.service.mappers.UserMapper;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Service
public class UserService {

    private static final String USER_NOT_FOUND = "User not found";

    private final ApplicationUsersRepository applicationUsersRepository;
    private final ApplicationHelper applicationHelper;
    private final UsersRepository usersRepository;
    private final RolesRepository rolesRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UsersRepository usersRepository, PasswordEncoder passwordEncoder, UserMapper userMapper,
                       ApplicationHelper applicationHelper, ApplicationUsersRepository applicationUsersRepository,
                       RolesRepository rolesRepository) {
        this.applicationUsersRepository = applicationUsersRepository;
        this.applicationHelper = applicationHelper;
        this.rolesRepository = rolesRepository;
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    @Transactional
    public Set<User> getAllUsers(String applicationUid) {
        log.debug("Getting all users from database.");
        Application application = applicationHelper.checkIfApplicationExists(applicationUid);
        return application.getUsers();
    }

    @Transactional
    public CheckUserExistenceResponse checkUserDataExistence(String applicationUid,
                                                             CheckUserExistenceRequest checkUserExistenceRequest) {
        applicationHelper.checkIfApplicationExists(applicationUid);

        log.debug("Checking user data existence: {}", checkUserExistenceRequest);
        CheckUserExistenceResponse checkUserExistenceResponse = new CheckUserExistenceResponse();
        if (!StringUtils.isEmpty(checkUserExistenceRequest.getUsername())) {
            try {
                User user = getUserByName(applicationUid, checkUserExistenceRequest.getUsername());
                checkUserExistenceResponse.setUsernameIsUnique(user == null);
            } catch (Exception exception) {
                checkUserExistenceResponse.setUsernameIsUnique(true);
            }
        }
        if (!StringUtils.isEmpty(checkUserExistenceRequest.getEmail())) {
            try {
                User user = getUserByName(applicationUid, checkUserExistenceRequest.getEmail());
                checkUserExistenceResponse.setEmailIsUnique(user == null);
            } catch (Exception exception) {
                checkUserExistenceResponse.setEmailIsUnique(true);
            }
        }
        return checkUserExistenceResponse;
    }

    @Transactional
    public User getUserByName(String applicationUid, String identifier) {
        log.debug("Get user by identifier: {}", identifier);

        checkIfUserIsPartOfApplication(applicationUid, identifier);

        if (isValidEmailAddress(identifier)) {
            return getUser(usersRepository.findByEmailIgnoreCase(identifier));
        } else {
            return getUser(usersRepository.findByUsernameIgnoreCase(identifier));
        }
    }

    @Transactional
    public User createUser(String applicationUid, CreateUserRequest createUserRequest) {
        log.debug("Creating user: {}", createUserRequest.getUsername());

        validateUserData(createUserRequest.getUsername(), createUserRequest.getEmail());
        Application application = applicationHelper.checkIfApplicationExists(applicationUid);

        return createUser(userMapper.mapCreateUser(createUserRequest), application);
    }

    @Transactional
    public User updateUserByUsername(String applicationUid, String existingUsername,
                                     UpdateUserRequest updateUserRequest, Boolean canChangeRole) {
        log.debug("Updating user with username: {} ", existingUsername);

        checkIfUserIsPartOfApplication(applicationUid, existingUsername);

        final User oldUser = usersRepository.findByUsernameIgnoreCase(existingUsername);
        validateIfObjectExists(oldUser == null, USER_NOT_FOUND);

        checkPasswordChange(updateUserRequest, oldUser);
        if (canChangeRole) {
            checkRolesChange(updateUserRequest, oldUser);
        }
        User updatedUser = userMapper.mapUpdateUser(UpdateUserWrapper.builder().user(oldUser)
                .request(updateUserRequest).build());
        return usersRepository.save(updatedUser);
    }

    @Transactional
    public void deleteUserByUsername(String applicationUid, String username) {
        log.debug("Deleting User with username {}", username);

        checkIfUserIsPartOfApplication(applicationUid, username);

        User user = usersRepository.findByUsernameIgnoreCase(username);
        validateIfObjectExists(user == null, USER_NOT_FOUND);

        applicationUsersRepository.deleteByUserId(user.getId());
        usersRepository.deleteById(user.getId());
    }

    @Transactional
    public User getUserByName(String identifier) {
        log.debug("Get user by identifier: {}", identifier);
        if (isValidEmailAddress(identifier)) {
            return getUser(usersRepository.findByEmailIgnoreCase(identifier));
        } else {
            return getUser(usersRepository.findByUsernameIgnoreCase(identifier));
        }
    }

    public void checkIfUserIsPartOfApplication(String uid, String identifier) {
        Application application = applicationHelper.checkIfApplicationExists(uid);
        Set<User> users = application.getUsers();
        User dbUser = getUserByName(identifier);

        boolean isInCorrectApplication = false;
        for (User user : users) {
            if (user.getId().equals(dbUser.getId())) {
                isInCorrectApplication = true;
                break;
            }
        }
        if (! isInCorrectApplication) {
            throw new NotFoundException("User not found in current application !");
        }
    }

    private User createUser(User user, Application application) {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Set<Role> roles = new HashSet<>();
        roles.add(application.getDefaultUserRole());
        user.setRoles(roles);
        User dbUser = usersRepository.save(user);
        applicationUsersRepository.save(ApplicationUser.builder().applicationId(application.getId())
                .userId(dbUser.getId()).build());
        return dbUser;
    }

    private void checkRolesChange(UpdateUserRequest updateUserRequest, User oldUser) {
        if (updateUserRequest.getRoles() != null && !updateUserRequest.getRoles().isEmpty()) {
            Set<Role> finalRoles = new HashSet<>();
            for (String role : updateUserRequest.getRoles()) {
                Role dbRole = rolesRepository.findByRole(role);
                validateIfObjectExists(dbRole == null, String.format("Role: %s not found !", role));
                finalRoles.add(dbRole);
            }
            oldUser.setRoles(finalRoles);
        }
    }

    private void checkPasswordChange(UpdateUserRequest updateUserRequest, User oldUser) {
        if (!StringUtils.isEmpty(updateUserRequest.getPassword()) && !oldUser.getPassword()
                .equals(updateUserRequest.getPassword())) {
            oldUser.setPassword(passwordEncoder.encode(updateUserRequest.getPassword()));
        }
    }

    private void validateIfObjectExists(boolean exists, String message) {
        if (exists) {
            throw new NotFoundException(message);
        }
    }

    private User getUser(User user) {
        validateIfObjectExists(user == null, USER_NOT_FOUND);
        return user;
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
