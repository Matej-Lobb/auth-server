package sk.mlobb.authserver.it;

import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import sk.mlobb.authserver.app.AuthServerApplication;
import sk.mlobb.authserver.model.Role;
import sk.mlobb.authserver.service.RoleService;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthServerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class RolesITest {

    @Autowired
    private RoleService roleService;

    @Test
    public void testUsersFlow() {
        Role role = roleService.addRole("TEST");

        Role roleByName = roleService.getRoleByName(role.getRole());

        roleService.deleteRole(roleByName.getRole());
    }
}
