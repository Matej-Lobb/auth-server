package sk.mlobb.authserver.service;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import sk.mlobb.authserver.db.UsersRepository;
import sk.mlobb.authserver.db.UsersRolesRepository;
import sk.mlobb.authserver.model.Role;
import sk.mlobb.authserver.model.User;
import sk.mlobb.authserver.model.UserRoles;
import sk.mlobb.authserver.model.exception.ConflictException;
import sk.mlobb.authserver.model.exception.NotFoundException;

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

    private final UsersRolesRepository usersRolesRepository;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UsersRepository usersRepository, UsersRolesRepository usersRolesRepository,
                       PasswordEncoder passwordEncoder) {
        this.usersRolesRepository = usersRolesRepository;
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<User> getAllUsers() {
        log.debug("Getting all users from database.");
        return usersRepository.findAll();
    }

    public User getUserByName(String identifier) throws NotFoundException {
        log.info("Get user by identifier: {}", identifier);
        if (isValidEmailAddress(identifier)) {
            return getUser(usersRepository.findByEmailIgnoreCase(identifier));
        } else {
            return getUser(usersRepository.findByUsernameIgnoreCase(identifier));
        }
    }

    public Optional<User> getUserById(long id) {
        log.info("Get user with id " + id);
        return usersRepository.findById(id);
    }

    public User createUser(User user, Role role) throws ConflictException {
        log.info("Creating user " + user.getUsername());
        checkIfUserExists(user);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user = usersRepository.save(user);

        usersRolesRepository.save(UserRoles.builder().userId(user.getId()).userRoleId(role.getId()).build());

        Set<Role> roles = new HashSet<>();
        roles.add(Role.builder().role(role.getRole()).build());
        user.setRoles(roles);
        return usersRepository.save(user);
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

    private void checkIfUserExists(User user) throws ConflictException {
        boolean username = usersRepository.findByUsernameIgnoreCase(user.getUsername()) != null;
        if (username) {
            throw new ConflictException("A user with name " + user.getUsername() + " already exist");
        }
        boolean email = usersRepository.findByEmailIgnoreCase(user.getEmail()) != null;
        if (email) {
            throw new ConflictException("A user with email " + user.getEmail() + " already exist");
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
        if (! StringUtils.isEmpty(newUser.getCountry())) {
            oldUser.setCountry(newUser.getCountry());
        }
        if (! StringUtils.isEmpty(newUser.getEmail())) {
            oldUser.setEmail(newUser.getEmail());
        }
        if (! StringUtils.isEmpty(newUser.getFirstName())) {
            oldUser.setFirstName(newUser.getFirstName());
        }
        if (! StringUtils.isEmpty(newUser.getLastName())) {
            oldUser.setLastName(newUser.getLastName());
        }
        if (! StringUtils.isEmpty(newUser.getPassword()) && !oldUser.getPassword().equals(newUser.getPassword())) {
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
