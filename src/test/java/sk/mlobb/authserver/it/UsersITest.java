package sk.mlobb.authserver.it;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import sk.mlobb.authserver.app.AuthServerApplication;
import sk.mlobb.authserver.model.exception.NotFoundException;
import sk.mlobb.authserver.model.rest.User;
import sk.mlobb.authserver.model.rest.request.CreateUserRequest;
import sk.mlobb.authserver.model.rest.request.UpdateUserRequest;
import sk.mlobb.authserver.rest.UserController;

import java.time.Duration;
import java.time.LocalDate;
import java.util.HashSet;

import static org.awaitility.Awaitility.with;
import static org.mockito.Mockito.when;

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

        userController.createUser(APPLICATION_UID, createUserRequest);

        when(authentication.getPrincipal()).thenReturn("test");

        User user = userController.getUserByName(APPLICATION_UID,
                createUserRequest.getUsername());
        Assert.assertNotNull(user);

        user = userController.updateUserByUsername(APPLICATION_UID, user.getUsername(),
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

        Assert.assertNotNull(user);
        Assert.assertEquals("new", user.getCountry());
        Assert.assertEquals("new@new.sk", user.getEmail());
        Assert.assertEquals("new", user.getFirstName());
        Assert.assertEquals("new", user.getLastName());
        Assert.assertEquals(2, user.getRoles().size());
        Assert.assertFalse(user.getKeepUpdated());
        Assert.assertNotNull(user.getPassword());

        final String username = user.getUsername();
        userController.deleteUserByName(APPLICATION_UID, username);

        with().pollInterval(Duration.ofSeconds(5)).and().with().pollDelay(Duration.ofSeconds(1)).await()
                .atMost(Duration.ofSeconds(30)).until(() -> {
            try {
                userController.getUserByName(APPLICATION_UID, username);
                return false;
            } catch (NotFoundException exception) {
                Assert.assertEquals("User not found", exception.getMessage());
                return true;
            }
        });
    }
}
