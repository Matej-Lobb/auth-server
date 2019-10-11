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
import org.springframework.web.util.UriComponentsBuilder;
import sk.mlobb.authserver.app.AuthServerApplication;
import sk.mlobb.authserver.model.User;
import sk.mlobb.authserver.model.rest.CreateUserRequest;
import sk.mlobb.authserver.model.rest.UpdateUserRequest;
import sk.mlobb.authserver.rest.UserController;

import java.time.LocalDate;

import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthServerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class UsersIT {

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

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();
        ResponseEntity<?> createUserResponse = userController.createUser(APPLICATION_UID, createUserRequest,
                uriComponentsBuilder);

        Assert.assertEquals(HttpStatus.CREATED, createUserResponse.getStatusCode());

        ResponseEntity<?> getUserResponse = userController.getUserByName(APPLICATION_UID,
                createUserRequest.getUsername());

        Assert.assertEquals(HttpStatus.OK, getUserResponse.getStatusCode());
        User user = (User) getUserResponse.getBody();
        Assert.assertNotNull(user);

        ResponseEntity<?> allUsersResponse = userController.getAllUsers(APPLICATION_UID);
        Assert.assertEquals(HttpStatus.UNAUTHORIZED, allUsersResponse.getStatusCode());

        ResponseEntity<?> updateUserResponse = userController.updateUserByUsername(APPLICATION_UID, user.getUsername(),
                UpdateUserRequest.builder()
                        .active(true)
                        .country("new")
                        .email("new@new.sk")
                        .firstName("new")
                        .lastName("new")
                        .keepUpdated(false)
                        .build());

        Assert.assertEquals(HttpStatus.OK, updateUserResponse.getStatusCode());
        user = (User) getUserResponse.getBody();
        Assert.assertNotNull(user);
        Assert.assertEquals("new", user.getCountry());
        Assert.assertEquals("new@new.sk", user.getEmail());
        Assert.assertEquals("new", user.getFirstName());
        Assert.assertEquals("new", user.getLastName());
        Assert.assertNotNull(user.getPassword());

        ResponseEntity<?> deleteUserResponse = userController.deleteUserByName(APPLICATION_UID, user.getUsername());
        Assert.assertEquals(HttpStatus.OK, deleteUserResponse.getStatusCode());

        getUserResponse = userController.getUserByName(APPLICATION_UID, user.getUsername());

        Assert.assertEquals(HttpStatus.NOT_FOUND, getUserResponse.getStatusCode());
    }
}
