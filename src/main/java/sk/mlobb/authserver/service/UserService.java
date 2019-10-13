package sk.mlobb.authserver.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sk.mlobb.authserver.db.UsersRepository;
import sk.mlobb.authserver.db.UsersRolesRepository;
import sk.mlobb.authserver.model.Application;
import sk.mlobb.authserver.model.Role;
import sk.mlobb.authserver.model.User;
import sk.mlobb.authserver.model.exception.ConflictException;
import sk.mlobb.authserver.model.exception.NotFoundException;
import sk.mlobb.authserver.model.rest.request.CheckUserExistenceRequest;
import sk.mlobb.authserver.model.rest.request.CreateUserRequest;
import sk.mlobb.authserver.model.rest.request.UpdateUserRequest;
import sk.mlobb.authserver.model.rest.response.CheckUserExistenceResponse;
import sk.mlobb.authserver.service.mappers.UserMapper;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
public class UserService {

    private static final String USER_NOT_FOUND = "User not found";

    private final UsersRolesRepository usersRolesRepository;
    private final ApplicationService applicationService;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UsersRepository usersRepository, PasswordEncoder passwordEncoder,
                       ApplicationService applicationService, UserMapper userMapper,
                       UsersRolesRepository usersRolesRepository) {
        this.usersRolesRepository = usersRolesRepository;
        this.applicationService = applicationService;
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public List<User> getAllUsers(String applicationUid) {
        log.debug("Getting all users from database.");
        final Application application = applicationService.getByUid(applicationUid);
        validateObject(application == null, "Application not exists !");
        return usersRepository.findAllByApplication(application);
    }

    public User getUserByName(String identifier) throws NotFoundException {
        log.debug("Get user by identifier: {}", identifier);
        if (isValidEmailAddress(identifier)) {
            return getUser(usersRepository.findByEmailIgnoreCase(identifier));
        } else {
            return getUser(usersRepository.findByUsernameIgnoreCase(identifier));
        }
    }

    public CheckUserExistenceResponse checkUserDataExistence(String applicationUid,
                                                             CheckUserExistenceRequest checkUserExistenceRequest) {
        checkIfApplicationExists(applicationUid);

        log.debug("Checking user data existence: {}", checkUserExistenceRequest);
        CheckUserExistenceResponse checkUserExistenceResponse = new CheckUserExistenceResponse();
        if (!StringUtils.isEmpty(checkUserExistenceRequest.getUsername())) {
            try {
                User user = getUserByName(applicationUid, checkUserExistenceRequest.getUsername());
                if (user != null) {
                    checkUserExistenceResponse.setUsernameIsUnique(false);
                } else {
                    checkUserExistenceResponse.setUsernameIsUnique(true);
                }
            } catch (Exception exception) {
                checkUserExistenceResponse.setUsernameIsUnique(true);
            }
        }
        if (!StringUtils.isEmpty(checkUserExistenceRequest.getEmail())) {
            try {
                User user = getUserByName(applicationUid, checkUserExistenceRequest.getEmail());
                if (user != null) {
                    checkUserExistenceResponse.setEmailIsUnique(false);
                } else {
                    checkUserExistenceResponse.setEmailIsUnique(true);
                }
            } catch (Exception exception) {
                checkUserExistenceResponse.setEmailIsUnique(true);
            }
        }
        return checkUserExistenceResponse;
    }

    public User getUserByName(String applicationUid, String identifier) throws NotFoundException {
        log.debug("Get user by identifier: {}", identifier);
        checkIfApplicationExists(applicationUid);
        if (isValidEmailAddress(identifier)) {
            return getUser(usersRepository.findByEmailIgnoreCase(identifier));
        } else {
            return getUser(usersRepository.findByUsernameIgnoreCase(identifier));
        }
    }

    public User createUser(String applicationUid, CreateUserRequest createUserRequest) throws ConflictException {
        log.debug("Creating user: {}", createUserRequest.getUsername());
        validateObject(createUserRequest.getUsername(), createUserRequest.getEmail());

        final Application application = applicationService.getByUid(applicationUid);
        validateObject(application == null, "Application not exists !");

        return createUser(userMapper.mapCreateUser(createUserRequest, application), application.getDefaultUserRole());
    }

    private User createUser(User user, Role role) throws ConflictException {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        user = usersRepository.save(user);
        return user;
    }

    // TODO Update Roles / License
    public User updateUserByUsername(String applicationUid, String existingUsername,
                                     UpdateUserRequest updateUserRequest) throws NotFoundException {
        log.debug("Updating user with username: {} ", existingUsername);

        Application application = checkIfApplicationExists(applicationUid);

        final User oldUser = usersRepository.findByUsernameIgnoreCase(existingUsername);
        validateObject(oldUser == null, USER_NOT_FOUND);

        User newUser = userMapper.mapUpdateUser(updateUserRequest, application);

        final User updatedUser = mapUpdateRequestToUser(oldUser, newUser);
        return usersRepository.save(updatedUser);
    }

    public void deleteUserByUsername(String applicationUid, String username) throws NotFoundException {
        log.debug("Deleting User with username {}", username);

        checkIfApplicationExists(applicationUid);

        User user = usersRepository.findByUsernameIgnoreCase(username);
        validateObject(user == null, USER_NOT_FOUND);
        usersRolesRepository.deleteById(user.getId());
        usersRepository.deleteById(user.getId());
    }

    private void validateObject(boolean exists, String userNotFound) {
        if (exists) {
            throw new NotFoundException(userNotFound);
        }
    }

    private Application checkIfApplicationExists(String applicationUid) {
        final Application application = applicationService.getByUid(applicationUid);
        validateObject(application == null, "Application not exists !");
        return application;
    }

    private User getUser(User user) throws NotFoundException {
        validateObject(user == null, USER_NOT_FOUND);
        return user;
    }

    private void validateObject(String requestUsername, String requestEmail) throws ConflictException {
        boolean username = usersRepository.findByUsernameIgnoreCase(requestUsername) != null;
        if (username) {
            throw new ConflictException(String.format("A user with name %s already exist", requestUsername));
        }
        boolean email = usersRepository.findByEmailIgnoreCase(requestEmail) != null;
        if (email) {
            throw new ConflictException(String.format("A user with email %s already exist", requestEmail));
        }
    }

    private User mapUpdateRequestToUser(User oldUser, User newUser) {
        if (newUser.getKeepUpdated() != null) {
            oldUser.setKeepUpdated(newUser.getKeepUpdated());
        }
        if (newUser.getActive() != null) {
            oldUser.setActive(newUser.getActive());
        }
        if (newUser.getDateOfBirth() != null) {
            oldUser.setDateOfBirth(newUser.getDateOfBirth());
        }
        if (!StringUtils.isEmpty(newUser.getCountry())) {
            oldUser.setCountry(newUser.getCountry());
        }
        if (!StringUtils.isEmpty(newUser.getEmail())) {
            oldUser.setEmail(newUser.getEmail());
        }
        if (!StringUtils.isEmpty(newUser.getFirstName())) {
            oldUser.setFirstName(newUser.getFirstName());
        }
        if (!StringUtils.isEmpty(newUser.getLastName())) {
            oldUser.setLastName(newUser.getLastName());
        }
        if (!StringUtils.isEmpty(newUser.getPassword()) && !oldUser.getPassword().equals(newUser.getPassword())) {
            oldUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
        }
        return oldUser;
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
