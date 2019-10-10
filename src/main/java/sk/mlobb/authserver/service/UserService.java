package sk.mlobb.authserver.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sk.mlobb.authserver.db.UsersRepository;
import sk.mlobb.authserver.model.Application;
import sk.mlobb.authserver.model.Role;
import sk.mlobb.authserver.model.User;
import sk.mlobb.authserver.model.exception.ConflictException;
import sk.mlobb.authserver.model.exception.NotFoundException;
import sk.mlobb.authserver.model.rest.CreateUserRequest;
import sk.mlobb.authserver.service.mappers.UserMapper;

import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Service
public class UserService {

    private static final String USER_NOT_FOUND = "User not found";

    private final ApplicationService applicationService;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Autowired
    public UserService(UsersRepository usersRepository, PasswordEncoder passwordEncoder,
                       ApplicationService applicationService, UserMapper userMapper) {
        this.applicationService = applicationService;
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.userMapper = userMapper;
    }

    public List<User> getAllUsers(String applicationUid) {
        log.debug("Getting all users from database.");
        final Application application = applicationService.getByUid(applicationUid);
        if (application == null) {
            throw new NotFoundException("Application not exists !");
        }
        return usersRepository.findAllByApplication(application);
    }

    public User getUserByName(String identifier) throws NotFoundException {
        log.info("Get user by identifier: {}", identifier);
        if (isValidEmailAddress(identifier)) {
            return getUser(usersRepository.findByEmailIgnoreCase(identifier));
        } else {
            return getUser(usersRepository.findByUsernameIgnoreCase(identifier));
        }
    }

    public User getUserByName(String applicationUid, String identifier) throws NotFoundException {
        log.info("Get user by identifier: {}", identifier);
        final Application application = applicationService.getByUid(applicationUid);
        if (application == null) {
            throw new NotFoundException("Application not exists !");
        }
        if (isValidEmailAddress(identifier)) {
            return getUser(usersRepository.findByEmailIgnoreCase(identifier));
        } else {
            return getUser(usersRepository.findByUsernameIgnoreCase(identifier));
        }
    }

    public User createUser(String applicationUid, CreateUserRequest createUserRequest) throws ConflictException {
        log.info("Creating user " + createUserRequest.getUsername());
        checkIfUserExists(createUserRequest.getUsername(), createUserRequest.getEmail());

        final Application application = applicationService.getByUid(applicationUid);
        if (application == null) {
            throw new NotFoundException("Application not exists !");
        }

        return createUser(userMapper.mapUser(createUserRequest, application), application.getDefaultUserRole());
    }

    private User createUser(User user, Role role) throws ConflictException {
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        Set<Role> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        user = usersRepository.save(user);
        return user;
    }

    public User updateUserById(long id, User newUser) throws NotFoundException {
        log.info("Updating user with id " + id);
        Optional<User> oldUser = usersRepository.findById(id);
        if (oldUser.isPresent()) {
            User updatedUser = mapUpdateRequestToUser(oldUser.get(), newUser);
            return usersRepository.save(updatedUser);
        }
        throw new NotFoundException(USER_NOT_FOUND);
    }

    public void deleteUserById(long id) throws NotFoundException {
        log.info("Deleting User with id " + id);
        Optional<User> user = usersRepository.findById(id);
        if (user.isPresent()) {
            usersRepository.deleteById(user.get().getId());
            return;
        }
        throw new NotFoundException(USER_NOT_FOUND);
    }

    private User getUser(User user) throws NotFoundException {
        if (user == null) {
            throw new NotFoundException(USER_NOT_FOUND);
        }
        return user;
    }

    private void checkIfUserExists(String requestUsername, String requestEmail) throws ConflictException {
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
        if (newUser.getRoles() != null && !newUser.getRoles().isEmpty()) {
            oldUser.setRoles(newUser.getRoles());
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
