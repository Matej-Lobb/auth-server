package sk.mlobb.authserver.rest;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    @GetMapping(value = "/applications/{applicationUid}/users")
    public List<User> getAllUsers(@PathVariable("applicationUid") String applicationUid) {
        return userService.getAllUsers(applicationUid);
    }

    @GetMapping(value = "/applications/{applicationUid}/users/{identifier}")
    public User getUserByName(@PathVariable("applicationUid") String applicationUid,
                                              @PathVariable("identifier") String identifier) {
        return userService.getUserByName(applicationUid, identifier);
    }

    @PostMapping(value = "/applications/{applicationUid}/users")
    @ResponseStatus(HttpStatus.CREATED)
    public User createUser(@PathVariable("applicationUid") String applicationUid,
                                     @Valid @RequestBody CreateUserRequest request) {
        return userService.createUser(applicationUid, request);
    }

    @PutMapping(value = "/applications/{applicationUid}/users/{identifier}")
    public User updateUserByUsername(@PathVariable("applicationUid") String applicationUid,
                                     @PathVariable("identifier") String identifier,
                                     @Valid @RequestBody UpdateUserRequest updateUserRequest) {
            return userService.updateUserByUsername(applicationUid, identifier, updateUserRequest, true);
    }

    @DeleteMapping(value = "/applications/{applicationUid}/users/{identifier}")
    public void deleteUserByName(@PathVariable("applicationUid") String applicationUid,
                                           @PathVariable("identifier") String identifier) {
        userService.deleteUserByUsername(applicationUid, identifier);
    }
}
