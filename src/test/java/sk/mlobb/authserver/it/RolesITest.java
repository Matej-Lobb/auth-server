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
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;
import sk.mlobb.authserver.app.AuthServerApplication;
import sk.mlobb.authserver.db.RolePermissionsRepository;
import sk.mlobb.authserver.model.Role;
import sk.mlobb.authserver.model.RolePermissions;
import sk.mlobb.authserver.model.exception.NotFoundException;
import sk.mlobb.authserver.model.permission.Permission;
import sk.mlobb.authserver.model.rest.request.CreateRoleRequest;
import sk.mlobb.authserver.model.rest.request.UpdateRoleRequest;
import sk.mlobb.authserver.rest.RoleController;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.when;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthServerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class RolesITest {

    private static final String ADMIN_ACCOUNT_NAME = "lobor";
    private static final String APPLICATION_UID = "1s2a1x";

    @Autowired
    private RoleController roleController;

    @Autowired
    private RolePermissionsRepository rolePermissionsRepository;

    @Test
    @Transactional
    public void testRolesFlow() {
        // get user with create access
        Authentication authentication = Mockito.mock(Authentication.class);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        when(authentication.getPrincipal()).thenReturn(ADMIN_ACCOUNT_NAME);

        UriComponentsBuilder uriComponentsBuilder = UriComponentsBuilder.newInstance();

        ResponseEntity createRoleResponse = roleController.createRole(APPLICATION_UID, CreateRoleRequest.builder()
                .roleName("test").build(), uriComponentsBuilder);
        Assert.assertNotNull(createRoleResponse);
        Assert.assertEquals(HttpStatus.CREATED, createRoleResponse.getStatusCode());

        ResponseEntity getRoleResponse = roleController.getRoleByName(APPLICATION_UID, "test");
        Assert.assertEquals(HttpStatus.OK, createRoleResponse.getStatusCode());
        Assert.assertNotNull(getRoleResponse.getBody());
        Role role = (Role) getRoleResponse.getBody();
        Assert.assertEquals("test", role.getRole());
        Assert.assertNotNull(role.getPermissions());
        log.info("Permissions: {}", role.getPermissions());

        ResponseEntity updateRoleResponse = roleController.updateRoleByName(APPLICATION_UID, role.getRole(),
                UpdateRoleRequest.builder().permissions(new HashSet<>() {{
                    add(UpdateRoleRequest.Permission.builder()
                            .nameAlias("get-application")
                            .access(UpdateRoleRequest.Permission.Access.builder()
                                    .readAll(true)
                                    .readSelf(true)
                                    .writeAll(true)
                                    .writeSelf(true)
                                    .build())
                            .build());

                    add(UpdateRoleRequest.Permission.builder()
                            .nameAlias("get-all-users")
                            .access(UpdateRoleRequest.Permission.Access.builder()
                                    .readAll(true)
                                    .readSelf(true)
                                    .writeAll(true)
                                    .writeSelf(true)
                                    .build())
                            .build());

                    add(UpdateRoleRequest.Permission.builder()
                            .nameAlias("get-user")
                            .access(UpdateRoleRequest.Permission.Access.builder()
                                    .readAll(true)
                                    .readSelf(true)
                                    .writeAll(true)
                                    .writeSelf(true)
                                    .build())
                            .build());
                }}).build());

        Assert.assertEquals(HttpStatus.OK, updateRoleResponse.getStatusCode());
        Assert.assertNotNull(updateRoleResponse.getBody());

        ResponseEntity updatedRoleResponse = roleController.getRoleByName(APPLICATION_UID, role.getRole());

        Assert.assertNotNull(updatedRoleResponse);
        Assert.assertEquals(HttpStatus.OK, updatedRoleResponse.getStatusCode());
        Assert.assertNotNull(updatedRoleResponse.getBody());
        Role updatedRole = (Role) updatedRoleResponse.getBody();

        Permission getUserPermission = getPermission("get-user", updatedRole.getPermissions());
        Assert.assertTrue(getUserPermission.getAccess().isReadAll());
        Assert.assertTrue(getUserPermission.getAccess().isReadSelf());
        Assert.assertTrue(getUserPermission.getAccess().isWriteAll());
        Assert.assertTrue(getUserPermission.getAccess().isWriteSelf());

        Permission getAllUsersPermission = getPermission("get-all-users", updatedRole.getPermissions());
        Assert.assertTrue(getAllUsersPermission.getAccess().isReadAll());
        Assert.assertTrue(getAllUsersPermission.getAccess().isReadSelf());
        Assert.assertTrue(getAllUsersPermission.getAccess().isWriteAll());
        Assert.assertTrue(getAllUsersPermission.getAccess().isWriteSelf());

        Permission getApplicationPermission = getPermission("get-application", updatedRole.getPermissions());
        Assert.assertTrue(getApplicationPermission.getAccess().isReadAll());
        Assert.assertTrue(getApplicationPermission.getAccess().isReadSelf());
        Assert.assertTrue(getApplicationPermission.getAccess().isWriteAll());
        Assert.assertTrue(getApplicationPermission.getAccess().isWriteSelf());

        ResponseEntity deleteRoleResponse = roleController.deleteRoleByName(APPLICATION_UID, updatedRole.getRole());
        Assert.assertNotNull(deleteRoleResponse);
        Assert.assertEquals(HttpStatus.OK, deleteRoleResponse.getStatusCode());

        List<RolePermissions> rolePermissions = rolePermissionsRepository.findAllByRoleId(role.getId());
        Assert.assertTrue(rolePermissions.isEmpty());

        try {
            roleController.getRoleByName(APPLICATION_UID, role.getRole());
        } catch (NotFoundException exception) {
            Assert.assertEquals("Role not found !", exception.getMessage());
            return;
        }
        Assert.fail();
    }

    private Permission getPermission(String nameAlias, Set<Permission> permissions) {
        Permission finalPermission = null;
        for (Permission permission : permissions) {
            if (permission.getNameAlias().equals(nameAlias)) {
                finalPermission = permission;
            }
        }
        if (finalPermission == null) {
            Assert.fail();
        }
        return finalPermission;
    }
}
