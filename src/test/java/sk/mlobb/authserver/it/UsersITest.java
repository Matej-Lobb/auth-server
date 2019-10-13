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
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.util.UriComponentsBuilder;
import sk.mlobb.authserver.app.AuthServerApplication;
import sk.mlobb.authserver.model.User;
import sk.mlobb.authserver.model.rest.request.CheckUserExistenceRequest;
import sk.mlobb.authserver.model.rest.request.CreateUserRequest;
import sk.mlobb.authserver.model.rest.request.UpdateUserRequest;
import sk.mlobb.authserver.model.rest.response.CheckUserExistenceResponse;
import sk.mlobb.authserver.rest.UserController;

import java.time.LocalDate;

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
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.getPrincipal()).thenReturn("test");

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
        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
        ResponseEntity<?> createUserResponse = userController.createUser(APPLICATION_UID, createUserRequest,
                uriComponentsBuilder);
        log.info("Response: {}", createUserResponse);
        Assert.assertEquals(HttpStatus.CREATED, createUserResponse.getStatusCode());

        log.info("Getting user: {}", createUserRequest.getUsername());
        ResponseEntity<?> getUserResponse = userController.getUserByName(APPLICATION_UID,
                createUserRequest.getUsername());
        log.info("Response: {}", getUserResponse);

        Assert.assertEquals(HttpStatus.OK, getUserResponse.getStatusCode());
        User user = (User) getUserResponse.getBody();
        Assert.assertNotNull(user);

        log.info("Updating user: {}", user.getUsername());
        ResponseEntity<?> updateUserResponse = userController.updateUserByUsername(APPLICATION_UID, user.getUsername(),
                UpdateUserRequest.builder()
                        .active(true)
                        .country("new")
                        .email("new@new.sk")
                        .firstName("new")
                        .lastName("new")
                        .keepUpdated(false)
                        .build());
        log.info("Response: {}", updateUserResponse);

        Assert.assertEquals(HttpStatus.OK, updateUserResponse.getStatusCode());
        user = (User) updateUserResponse.getBody();
        Assert.assertNotNull(user);
        Assert.assertEquals("new", user.getCountry());
        Assert.assertEquals("new@new.sk", user.getEmail());
        Assert.assertEquals("new", user.getFirstName());
        Assert.assertEquals("new", user.getLastName());
        Assert.assertFalse(user.getKeepUpdated());
        Assert.assertNotNull(user.getPassword());

        ResponseEntity<?> checkUserDataResponse = userController.checkUserDataExistence(APPLICATION_UID,
                CheckUserExistenceRequest.builder().username("test").email("new@new.sk").build());

        Assert.assertEquals(HttpStatus.OK, checkUserDataResponse.getStatusCode());
        CheckUserExistenceResponse checkUserExistenceResponse = (CheckUserExistenceResponse)
                checkUserDataResponse.getBody();
        Assert.assertNotNull(checkUserExistenceResponse);
        Assert.assertFalse(checkUserExistenceResponse.getEmailIsUnique());
        Assert.assertFalse(checkUserExistenceResponse.getUsernameIsUnique());

        log.info("Deleting user: {}", user.getUsername());
        ResponseEntity<?> deleteUserResponse = userController.deleteUserByName(APPLICATION_UID, user.getUsername());
        Assert.assertEquals(HttpStatus.OK, deleteUserResponse.getStatusCode());
        log.info("Response: {}", deleteUserResponse);

        try {
            log.info("Getting user: {}", user.getUsername());
            userController.getUserByName(APPLICATION_UID, user.getUsername());
        } catch (Exception exception) {
            if (exception instanceof UsernameNotFoundException) {
                Assert.assertEquals("User not exist in current context!", exception.getMessage());
            } else {
                Assert.fail();
            }
        }
    }
}
