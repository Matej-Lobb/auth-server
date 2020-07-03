package sk.mlobb.authserver.it;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import sk.mlobb.authserver.app.AuthServerApplication;
import sk.mlobb.authserver.model.exception.NotFoundException;
import sk.mlobb.authserver.model.rest.request.CreateUserRequest;
import sk.mlobb.authserver.model.rest.request.UpdateUserRequest;
import sk.mlobb.authserver.model.rest.User;
import sk.mlobb.authserver.rest.UserController;

import java.time.LocalDate;
import java.util.HashSet;

import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthServerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UsersITest {

    private static final String APPLICATION_UID = "1s2a1x";

    @Autowired
    private UserController userController;

    @Test
    public void testUsersFlow() {
        // get user with create access
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.getPrincipal()).thenReturn("lobor");

        CreateUserRequest createUserRequest = CreateUserRequest.builder()
                .active(true)
                .country("Slovakia")
                .dateOfBirth(LocalDate.now())
                .email("test@test.sk")
                .firstName("test")
                .lastName("test")
                .keepUpdated(true)
                .password("test")
                .username("test")
                .build();

        log.info("Creating user: {}", createUserRequest.toString());
        ResponseEntity<?> createUserResponse = userController.createUser(APPLICATION_UID, createUserRequest);
        log.info("Response: {}", createUserResponse);
        Assert.assertEquals(HttpStatus.CREATED, createUserResponse.getStatusCode());

        // get created user
        when(authentication.getPrincipal()).thenReturn("test");

        log.info("Getting user: {}", createUserRequest.getUsername());
        ResponseEntity<User> getUserResponse = userController.getUserByName(APPLICATION_UID,
                createUserRequest.getUsername());
        log.info("Response: {}", getUserResponse);

        Assert.assertEquals(HttpStatus.OK, getUserResponse.getStatusCode());
        User user = getUserResponse.getBody();
        Assert.assertNotNull(user);

        log.info("Updating user: {}", user.getUsername());
        ResponseEntity<User> updateUserResponse = userController.updateUserByUsername(APPLICATION_UID, user.getUsername(),
                UpdateUserRequest.builder()
                        .active(true)
                        .country("new")
                        .password("new")
                        .email("new@new.sk")
                        .firstName("new")
                        .lastName("new")
                        .keepUpdated(false)
                        .roles(new HashSet<>() {{
                            add("USER");
                            add("ADMIN");
                        }})
                        .build());
        log.info("Response: {}", updateUserResponse);

        Assert.assertEquals(HttpStatus.OK, updateUserResponse.getStatusCode());
        user = updateUserResponse.getBody();
        Assert.assertNotNull(user);
        Assert.assertEquals("new", user.getCountry());
        Assert.assertEquals("new@new.sk", user.getEmail());
        Assert.assertEquals("new", user.getFirstName());
        Assert.assertEquals("new", user.getLastName());
        Assert.assertEquals(1, user.getRoles().size());
        Assert.assertFalse(user.getKeepUpdated());
        Assert.assertNotNull(user.getPassword());

        log.info("Deleting user: {}", user.getUsername());
        ResponseEntity<?> deleteUserResponse = userController.deleteUserByName(APPLICATION_UID, user.getUsername());
        Assert.assertEquals(HttpStatus.OK, deleteUserResponse.getStatusCode());
        log.info("Response: {}", deleteUserResponse);

        try {
            log.info("Getting user: {}", user.getUsername());
            userController.getUserByName(APPLICATION_UID, user.getUsername());
        } catch (NotFoundException exception) {
            Assert.assertEquals("User not found", exception.getMessage());
            return;
        }
        Assert.fail();
    }
}
