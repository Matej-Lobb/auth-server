package sk.mlobb.authserver.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import sk.mlobb.authserver.model.User;
import sk.mlobb.authserver.model.rest.CreateUserRequest;
import sk.mlobb.authserver.model.rest.UpdateUserRequest;
import sk.mlobb.authserver.rest.auth.RestAuthenticationHandler;
import sk.mlobb.authserver.service.UserService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
public class UserController {

    private final RestAuthenticationHandler restAuthenticationHandler;
    private final UserService userService;

    public UserController(RestAuthenticationHandler restAuthenticationHandler, UserService userService) {
        this.restAuthenticationHandler = restAuthenticationHandler;
        this.userService = userService;
    }

    @Secured("ADMIN")
    @GetMapping(value = "/applications/{applicationUid}/users",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> getAllUsers(@PathVariable("applicationUid") String applicationUid) {
        restAuthenticationHandler.checkAccess();
        final List<User> users = userService.getAllUsers(applicationUid);
        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }


    @GetMapping(value = {"/applications/{applicationUid}/users/{identifier}"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> getUserByName(@PathVariable("applicationUid") String applicationUid,
                                           @PathVariable("identifier") String identifier) {
        if (restAuthenticationHandler.isAdminAccess()) {
            return getUser(userService.getUserByName(applicationUid, identifier));
        } else {
            // Allow only self User Data not others
            final User userFromContext = restAuthenticationHandler.getUserFromContext();
            if (userFromContext.getUsername().equals(identifier)) {
                return getUser(userService.getUserByName(applicationUid, identifier));
            }
            if (userFromContext.getEmail().equals(identifier)) {
                return getUser(userService.getUserByName(applicationUid, identifier));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping(value = {"/applications/{applicationUid}/users"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> createUser(@PathVariable("applicationUid") String applicationUid,
                                        @Valid @RequestBody CreateUserRequest request, UriComponentsBuilder ucBuilder) {
        log.info("Creating user " + request.getUsername());
        final User dbUser = userService.createUser(applicationUid, request);
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/applications/{applicationUid}/users/{identifier}")
                .buildAndExpand(dbUser.getApplication().getId(), dbUser.getUsername()).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @PutMapping(value = {"/applications/{applicationUid}/users/{username}"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> updateUserByUsername(@PathVariable("applicationUid") String applicationUid,
                                                  @PathVariable("username") String username,
                                                  @RequestBody UpdateUserRequest updateUserRequest) {
        if (restAuthenticationHandler.isAdminAccess()) {
            return getUser(userService.updateUserByUsername(applicationUid, username, updateUserRequest));
        } else {
            // Allow only self User Data not others
            final User userFromContext = restAuthenticationHandler.getUserFromContext();
            if (userFromContext.getUsername().equals(username)) {
                return getUser(userService.updateUserByUsername(applicationUid, username, updateUserRequest));
            }
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @DeleteMapping(value = {"/applications/{applicationUid}/users/{username}"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> deleteUserByName(@PathVariable("applicationUid") String applicationUid,
                                              @PathVariable("username") String username) {
        if (restAuthenticationHandler.isAdminAccess()) {
            userService.deleteUserByUsername(applicationUid, username);
        } else {
            // Allow only self User Data not others
            final User userFromContext = restAuthenticationHandler.getUserFromContext();
            if (userFromContext.getUsername().equals(username)) {
                userService.deleteUserByUsername(applicationUid, username);
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private ResponseEntity<?> getUser(User user) {
        if (user == null) {
            log.debug("User not found");
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
