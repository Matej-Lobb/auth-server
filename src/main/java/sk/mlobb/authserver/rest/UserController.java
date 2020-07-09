package sk.mlobb.authserver.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import sk.mlobb.authserver.model.rest.request.CreateUserRequest;
import sk.mlobb.authserver.model.rest.request.UpdateUserRequest;
import sk.mlobb.authserver.model.rest.User;
import sk.mlobb.authserver.service.UserService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping(value = "/applications/{applicationUid}/users",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<List<User>> getAllUsers(@PathVariable("applicationUid") String applicationUid) {
        final List<User> users = userService.getAllUsers(applicationUid);
        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @GetMapping(value = {"/applications/{applicationUid}/users/{identifier}"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<User> getUserByName(@PathVariable("applicationUid") String applicationUid,
                                              @PathVariable("identifier") String identifier) {
        return ResponseEntity.ok(userService.getUserByName(applicationUid, identifier));
    }

    @PostMapping(value = {"/applications/{applicationUid}/users"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<User> createUser(@PathVariable("applicationUid") String applicationUid,
                                     @Valid @RequestBody CreateUserRequest request) {
        log.info("Creating user " + request.getUsername());
        final User user = userService.createUser(applicationUid, request);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @PutMapping(value = {"/applications/{applicationUid}/users/{identifier}"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<User> updateUserByUsername(@PathVariable("applicationUid") String applicationUid,
                                     @PathVariable("identifier") String identifier,
                                     @Valid @RequestBody UpdateUserRequest updateUserRequest) {
            return ResponseEntity.ok(userService.updateUserByUsername(applicationUid, identifier, updateUserRequest,
                    true));
    }

    @DeleteMapping(value = {"/applications/{applicationUid}/users/{identifier}"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<Void> deleteUserByName(@PathVariable("applicationUid") String applicationUid,
                                           @PathVariable("identifier") String identifier) {
        userService.deleteUserByUsername(applicationUid, identifier);
        return ResponseEntity.ok().build();
    }
}
