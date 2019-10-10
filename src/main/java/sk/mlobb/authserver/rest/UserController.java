package sk.mlobb.authserver.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import sk.mlobb.authserver.model.User;
import sk.mlobb.authserver.model.rest.CreateUserRequest;
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
    @GetMapping(value = "/applications/{applicationUid}/users/all",
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

    @PostMapping(value = {"/applications/{applicationId}/users/create"},
            produces = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE},
            consumes = {MediaType.APPLICATION_JSON_VALUE, MediaType.APPLICATION_XML_VALUE})
    public ResponseEntity<?> createUser(@PathVariable("applicationId") String applicationId,
                                        @Valid @RequestBody CreateUserRequest request, UriComponentsBuilder ucBuilder) {
        log.info("Creating user " + request.getUsername());
        final User dbUser = userService.createUser(applicationId, request);
        final HttpHeaders headers = new HttpHeaders();
        headers.setLocation(ucBuilder.path("/applications/{applicationUid}/users/{identifier}")
                .buildAndExpand(dbUser.getApplication().getId(), dbUser.getUsername()).toUri());
        return new ResponseEntity<>(headers, HttpStatus.CREATED);
    }

//    /**
//     * Update user by id response entity.
//     *
//     * @param id         the id
//     * @param updateUser the update user
//     * @return the response entity
//     */
//    @RequestMapping(value = {"/user/update/{id}"}, method = RequestMethod.PUT)
//    public ResponseEntity<?> updateUserById(@PathVariable("id") long id,
//                                            @RequestBody User updateUser) {
//        LOG.info("Updating user with id " + id);
//        final User oldUser = userService.findById(id);
//        if (oldUser == null) {
//            LOG.info("Unable to update user. User not found");
//            return new ResponseEntity<>(new ErrorResponse().setError(HttpStatus.NOT_FOUND.getReasonPhrase())
//                    .setError_description("Unable to update user. User not found"), HttpStatus.NOT_FOUND);
//        }
//        final boolean authorized = ScaffoldRestUtils.checkAuthorization(oldUser.getUsername());
//        if (!authorized) {
//            return new ResponseEntity<>(new ErrorResponse().setError(HttpStatus.UNAUTHORIZED.getReasonPhrase())
//                    .setError_description("You don't have correct access right for this call"),
//                    HttpStatus.UNAUTHORIZED);
//        }
//        userService.updateUser(oldUser, updateUser);
//        return new ResponseEntity<>(HttpStatus.OK);
//    }
//
//    /**
//     * Delete user by id response entity.
//     *
//     * @param id the id
//     * @return the response entity
//     */
//    @RequestMapping(value = {"/user/delete/{id}"}, method = RequestMethod.DELETE)
//    public ResponseEntity<?> deleteUserById(@PathVariable("id") long id) {
//        LOG.info("Deleting User with id " + id);
//        User user = userService.findById(id);
//        if (user == null) {
//            LOG.info("Unable to delete. User not found");
//            return new ResponseEntity<>(new ErrorResponse().setError(HttpStatus.NOT_FOUND.getReasonPhrase())
//                    .setError_description("Unable to delete. User not found"), HttpStatus.NOT_FOUND);
//        }
//        final boolean authorized = ScaffoldRestUtils.checkAuthorization(user.getUsername());
//        if (!authorized) {
//            return new ResponseEntity<>(new ErrorResponse().setError(HttpStatus.UNAUTHORIZED.getReasonPhrase())
//                    .setError_description("You don't have correct access right for this call"),
//                    HttpStatus.UNAUTHORIZED);
//        }
//        userService.deleteUserById(user.getId());
//        return new ResponseEntity<>(HttpStatus.OK);
//    }

    private ResponseEntity<?> getUser(User user) {
        if (user == null) {
            log.debug("User not found");
            return ResponseEntity.notFound().build();
        }
        return new ResponseEntity<>(user, HttpStatus.OK);
    }
}
