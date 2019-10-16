package sk.mlobb.authserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sk.mlobb.authserver.db.PermissionsRepository;
import sk.mlobb.authserver.db.RolePermissionsRepository;
import sk.mlobb.authserver.db.RolesRepository;
import sk.mlobb.authserver.model.Role;
import sk.mlobb.authserver.model.RolePermissions;
import sk.mlobb.authserver.model.exception.NotFoundException;
import sk.mlobb.authserver.model.permission.DefaultPermission;
import sk.mlobb.authserver.model.permission.Access;
import sk.mlobb.authserver.model.permission.Permission;
import sk.mlobb.authserver.model.permission.PermissionAlias;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
@Transactional
public class RoleService {

    private final RolePermissionsRepository rolePermissionsRepository;
    private final PermissionsRepository permissionsRepository;
    private final ApplicationContext applicationContext;
    private final RolesRepository rolesRepository;

    private Object[] acceptedAnnotations = {GetMapping.class, PostMapping.class, DeleteMapping.class,
            PutMapping.class, PatchMapping.class, RequestMapping.class};

    public RoleService(ApplicationContext applicationContext, RolesRepository rolesRepository,
                       RolePermissionsRepository rolePermissionsRepository,
                       PermissionsRepository permissionsRepository) {
        this.rolePermissionsRepository = rolePermissionsRepository;
        this.permissionsRepository = permissionsRepository;
        this.applicationContext = applicationContext;
        this.rolesRepository = rolesRepository;
    }

    public Role addRole(String roleName) {
        Role dbRole = rolesRepository.save(Role.builder().role(roleName).build());
        Set<RolePermissions> rolePermissions = new HashSet<>();
        for (Permission defaultPermission : getDefaultPermissions()) {
            Permission dbPermission = permissionsRepository.save(defaultPermission);
            rolePermissions.add(RolePermissions.builder().permissionId(dbPermission.getId()).roleId(dbRole.getId())
                    .build());
        }
        for (RolePermissions rolePermission: rolePermissions) {
            rolePermissionsRepository.save(rolePermission);
        }
        return rolesRepository.findByRole(dbRole.getRole());
    }

    public void deleteRole(String roleName) {
        rolesRepository.deleteByRole(roleName);
    }

    public Role getRoleByName(String role) {
        Role dbRole = rolesRepository.findByRole(role);
        if (dbRole == null) {
            throw new NotFoundException("Role not found !");
        }
        return dbRole;
    }

    private Set<Permission> getDefaultPermissions() {
        Set<Permission> permissions = new HashSet<>();
        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(RestController.class);
        for (Object controller : beansWithAnnotation.values()) {
            Method[] declaredMethods = controller.getClass().getDeclaredMethods();
            for (Method method : declaredMethods) {
                for (Annotation annotation : method.getDeclaredAnnotations()) {
                    for (Object object : acceptedAnnotations) {
                        if (annotation.annotationType().equals(object)) {
                            permissions.add(buildPermission(controller, method, object));
                        }
                    }
                }
            }
        }
        return permissions;
    }

    private Permission buildPermission(Object controller, Method method, Object annotation) {
        log.debug("Building Permission for method: {}", method.getName());
        Permission permission = Permission.builder().methodName(method.getName())
                .controller(controller.getClass().getSimpleName()).annotation(annotation.getClass().getSimpleName())
                .build();
        DefaultPermission definedPermission = AnnotationUtils.getAnnotation(method, DefaultPermission.class);
        if (definedPermission == null) {
            log.warn("No default permissions for method: {} in controller: {}", method.getName(),
                    controller.getClass().getSimpleName());
            permission.setAccess(Access.builder().readAll(false).readSelf(false).writeAll(false).writeSelf(false)
                    .build());
        } else {
            log.debug("Setting configured permissions for method: {} permissions: {}", method.getName(),
                    definedPermission);
            permission.setAccess(Access.builder()
                    .readAll(definedPermission.readAll())
                    .readSelf(definedPermission.readSelf())
                    .writeSelf(definedPermission.writeSelf())
                    .writeAll(definedPermission.writeAll())
                    .build());
        }
        PermissionAlias permissionAlias = AnnotationUtils.getAnnotation(method, PermissionAlias.class);
        if (permissionAlias == null) {
            permission.setNameAlias(permission.getMethodName());
        } else {
            log.debug("Permission alias found: {}", permissionAlias.value());
            permission.setNameAlias(permissionAlias.value());
        }
        return permission;
    }
}
