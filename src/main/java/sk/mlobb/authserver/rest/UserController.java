package sk.mlobb.authserver.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
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
import org.springframework.web.util.UriComponentsBuilder;
import sk.mlobb.authserver.model.User;
import sk.mlobb.authserver.model.permission.DefaultPermission;
import sk.mlobb.authserver.model.permission.PermissionAlias;
import sk.mlobb.authserver.model.rest.request.CheckUserExistenceRequest;
import sk.mlobb.authserver.model.rest.request.CreateUserRequest;
import sk.mlobb.authserver.model.rest.request.UpdateUserRequest;
import sk.mlobb.authserver.rest.auth.RestAuthenticationHandler;
import sk.mlobb.authserver.service.UserService;

import javax.validation.Valid;
import java.util.Set;

@Slf4j
@RestController
public class UserController {

    private final RestAuthenticationHandler restAuthenticationHandler;
    private final UserService userService;

    public UserController(RestAuthenticationHandler restAuthenticationHandler, UserService userService) {
        this.restAuthenticationHandler = restAuthenticationHandler;
        this.userService = userService;
    }

    @DefaultPermission
    @PermissionAlias("get-all-users")
    @GetMapping(value = "/applications/{applicationUid}/users",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity getAllUsers(@PathVariable("applicationUid") String applicationUid) {
        restAuthenticationHandler.checkAccess();
        final Set<User> users = userService.getAllUsers(applicationUid);
        if (users.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @DefaultPermission
    @PermissionAlias("check-user-exist")
    @PostMapping(value = {"/applications/{applicationUid}/users/check"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity checkUserDataExistence(@PathVariable("applicationUid") String applicationUid,
                                                 @Valid @RequestBody CheckUserExistenceRequest checkUserExistenceRequest) {
        restAuthenticationHandler.checkAccess();
        return ResponseEntity.ok(userService.checkUserDataExistence(applicationUid, checkUserExistenceRequest));
    }

    @DefaultPermission(readSelf = true)
    @PermissionAlias("get-user")
    @GetMapping(value = {"/applications/{applicationUid}/users/{identifier}"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity getUserByName(@PathVariable("applicationUid") String applicationUid,
                                        @PathVariable("identifier") String identifier) {
        restAuthenticationHandler.checkAccess();
        return getUser(userService.getUserByName(applicationUid, identifier));
    }

    @DefaultPermission
    @PermissionAlias("create-user")
    @PostMapping(value = {"/applications/{applicationUid}/users"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity createUser(@PathVariable("applicationUid") String applicationUid,
                                     @Valid @RequestBody CreateUserRequest request, UriComponentsBuilder ucBuilder) {
        restAuthenticationHandler.checkAccess();
        log.info("Creating user " + request.getUsername());
        final User dbUser = userService.createUser(applicationUid, request);
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/applications/{applicationUid}/users/{identifier}")
                .buildAndExpand(applicationUid, dbUser.getUsername()).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @DefaultPermission(readSelf = true, writeSelf = true)
    @PermissionAlias("update-user")
    @PutMapping(value = {"/applications/{applicationUid}/users/{username}"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity updateUserByUsername(@PathVariable("applicationUid") String applicationUid,
                                               @PathVariable("username") String username,
                                               @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        restAuthenticationHandler.checkAccess();
        return getUser(userService.updateUserByUsername(applicationUid, username, updateUserRequest, true));
    }

    @DefaultPermission(readSelf = true, writeSelf = true)
    @PermissionAlias("delete-user")
    @DeleteMapping(value = {"/applications/{applicationUid}/users/{username}"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity deleteUserByName(@PathVariable("applicationUid") String applicationUid,
                                           @PathVariable("username") String username) {
        restAuthenticationHandler.checkAccess();
        userService.deleteUserByUsername(applicationUid, username);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    private ResponseEntity getUser(User user) {
        if (user == null) {
            log.debug("User not found");
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
