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
import sk.mlobb.authserver.model.annotation.DefaultPermission;
import sk.mlobb.authserver.model.annotation.PermissionAlias;
import sk.mlobb.authserver.model.rest.request.CheckUserExistenceRequest;
import sk.mlobb.authserver.model.rest.request.CreateUserRequest;
import sk.mlobb.authserver.model.rest.request.UpdateUserRequest;
import sk.mlobb.authserver.rest.auth.AuthorizationHandler;
import sk.mlobb.authserver.service.UserService;

import javax.validation.Valid;
import java.util.Set;

import static sk.mlobb.authserver.model.enums.RequiredAccess.READ_ALL;
import static sk.mlobb.authserver.model.enums.RequiredAccess.READ_SELF;
import static sk.mlobb.authserver.model.enums.RequiredAccess.WRITE_ALL;
import static sk.mlobb.authserver.model.enums.RequiredAccess.WRITE_SELF;

@Slf4j
@RestController
public class UserController {

    private final AuthorizationHandler authorizationHandler;
    private final UserService userService;

    public UserController(AuthorizationHandler authorizationHandler, UserService userService) {
        this.authorizationHandler = authorizationHandler;
        this.userService = userService;
    }

    @DefaultPermission
    @PermissionAlias("get-all-users")
    @GetMapping(value = "/applications/{applicationUid}/users",
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity getAllUsers(@PathVariable("applicationUid") String applicationUid) {
        authorizationHandler.validateAccess(READ_ALL);
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
        authorizationHandler.validateAccess(READ_ALL, WRITE_ALL);
        return ResponseEntity.ok(userService.checkUserDataExistence(applicationUid, checkUserExistenceRequest));
    }

    @DefaultPermission(readSelf = true)
    @PermissionAlias("get-user")
    @GetMapping(value = {"/applications/{applicationUid}/users/{identifier}"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity getUserByName(@PathVariable("applicationUid") String applicationUid,
                                        @PathVariable("identifier") String identifier) {
        if (authorizationHandler.checkIfAccessingOwnUserData(identifier)) {
            authorizationHandler.validateAccess(READ_SELF);
        } else {
            authorizationHandler.validateAccess(READ_ALL);
        }
        return getUser(userService.getUserByName(applicationUid, identifier));
    }

    @DefaultPermission
    @PermissionAlias("create-user")
    @PostMapping(value = {"/applications/{applicationUid}/users"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity createUser(@PathVariable("applicationUid") String applicationUid,
                                     @Valid @RequestBody CreateUserRequest request, UriComponentsBuilder ucBuilder) {
        authorizationHandler.validateAccess(READ_ALL, WRITE_ALL);
        final User dbUser = userService.createUser(applicationUid, request);
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/applications/{applicationUid}/users/{identifier}")
                .buildAndExpand(applicationUid, dbUser.getUsername()).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

    @DefaultPermission(readSelf = true, writeSelf = true)
    @PermissionAlias("update-user")
    @PutMapping(value = {"/applications/{applicationUid}/users/{identifier}"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity updateUserByUsername(@PathVariable("applicationUid") String applicationUid,
                                               @PathVariable("identifier") String identifier,
                                               @Valid @RequestBody UpdateUserRequest updateUserRequest) {
        if (authorizationHandler.checkIfAccessingOwnUserData(identifier)) {
            authorizationHandler.validateAccess(WRITE_SELF, READ_SELF);
            return getUser(userService.updateUserByUsername(applicationUid, identifier, updateUserRequest,
                    false));
        } else {
            authorizationHandler.validateAccess(WRITE_ALL, READ_ALL);
            return getUser(userService.updateUserByUsername(applicationUid, identifier, updateUserRequest,
                    true));
        }
    }

    @DefaultPermission(readSelf = true, writeSelf = true)
    @PermissionAlias("delete-user")
    @DeleteMapping(value = {"/applications/{applicationUid}/users/{identifier}"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity deleteUserByName(@PathVariable("applicationUid") String applicationUid,
                                           @PathVariable("identifier") String identifier) {
        if (authorizationHandler.checkIfAccessingOwnUserData(identifier)) {
            authorizationHandler.validateAccess(WRITE_SELF);
        } else {
            authorizationHandler.validateAccess(WRITE_ALL);
        }
        userService.deleteUserByUsername(applicationUid, identifier);
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
