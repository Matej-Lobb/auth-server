package sk.mlobb.authserver.it;

import lombok.extern.slf4j.Slf4j;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import sk.mlobb.authserver.app.AuthServerApplication;
import sk.mlobb.authserver.db.RolePermissionsRepository;
import sk.mlobb.authserver.model.Role;
import sk.mlobb.authserver.model.RolePermissions;
import sk.mlobb.authserver.model.exception.NotFoundException;
import sk.mlobb.authserver.model.permission.Permission;
import sk.mlobb.authserver.model.rest.request.UpdateRoleRequest;
import sk.mlobb.authserver.service.RoleService;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest(classes = AuthServerApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class RolesITest {

    @Autowired
    private RoleService roleService;

    @Autowired
    private RolePermissionsRepository rolePermissionsRepository;

    @Test
    public void testRolesFlow() {
        Role role = roleService.addRole("test");
        Assert.assertNotNull(role);
        Assert.assertEquals("test", role.getRole());
        Assert.assertNotNull(role.getPermissions());
        log.info("Permissions: {}", role.getPermissions());

        roleService.updateRole(role.getRole(), UpdateRoleRequest.builder()
                .permissions(new HashSet<>() {{
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
                }})
                .build());

        Role updatedRole = roleService.getRoleByName(role.getRole());
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

        roleService.deleteRole(updatedRole.getRole());

        List<RolePermissions> rolePermissions = rolePermissionsRepository.findAllByRoleId(role.getId());
        Assert.assertTrue(rolePermissions.isEmpty());

        try {
            roleService.getRoleByName(role.getRole());
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
