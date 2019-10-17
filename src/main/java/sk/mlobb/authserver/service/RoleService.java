package sk.mlobb.authserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import sk.mlobb.authserver.db.RolesRepository;
import sk.mlobb.authserver.model.Role;
import sk.mlobb.authserver.model.annotation.DefaultPermission;
import sk.mlobb.authserver.model.annotation.PermissionAlias;
import sk.mlobb.authserver.model.exception.NotFoundException;
import sk.mlobb.authserver.model.permission.Access;
import sk.mlobb.authserver.model.permission.Permission;
import sk.mlobb.authserver.model.rest.request.UpdateRoleRequest;
import sk.mlobb.authserver.service.mappers.RoleMapper;
import sk.mlobb.authserver.service.mappers.UpdateRoleWrapper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@Service
public class RoleService {

    private final ApplicationContext applicationContext;
    private final RolesRepository rolesRepository;
    private final RoleMapper roleMapper;

    private Object[] acceptedAnnotations = {GetMapping.class, PostMapping.class, DeleteMapping.class,
            PutMapping.class, PatchMapping.class, RequestMapping.class};

    public RoleService(ApplicationContext applicationContext, RolesRepository rolesRepository, RoleMapper roleMapper) {
        this.applicationContext = applicationContext;
        this.rolesRepository = rolesRepository;
        this.roleMapper = roleMapper;
    }

    @Transactional
    public Role addRole(String roleName) {
        log.debug("Adding role: {}", roleName);
        return rolesRepository.save(Role.builder().role(roleName).permissions(getDefaultPermissions()).build());
    }

    @Transactional
    public Role getRoleByName(String role) {
        log.debug("Getting role: {}", role);
        Role dbRole = rolesRepository.findByRole(role);
        if (dbRole == null) {
            throw new NotFoundException("Role not found !");
        }
        return dbRole;
    }

    @Transactional
    public void updateRole(String role, UpdateRoleRequest updateRoleRequest) {
        log.debug("Updating role: {}", role);
        Role dbRole = roleMapper.mapUpdateRole(UpdateRoleWrapper.builder().role(getRoleByName(role))
                .request(updateRoleRequest).build());
        rolesRepository.save(dbRole);
    }

    @Transactional
    public void deleteRole(String roleName) {
        log.debug("Deleting role: {}", roleName);
        Role role = getRoleByName(roleName);
        rolesRepository.delete(role);
    }

    @Transactional
    public Permission getDefaultPermission(String controller, String method) {
        Set<Permission> defaultPermissions = getDefaultPermissions();
        for (Permission permission: defaultPermissions) {
            if (permission.getController().equals(controller) && permission.getMethodName().equals(method)) {
                return permission;
            }
        }
        throw new NotFoundException("Controller and method not found !");
    }

    private Set<Permission> getDefaultPermissions() {
        log.debug("Building default permissions !");
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
        log.debug("Default permissions: {}", permissions);
        return permissions;
    }

    private Permission buildPermission(Object controller, Method method, Object annotation) {
        log.debug("Building Permission for method: {}", method.getName());
        Permission permission = Permission.builder().methodName(method.getName())
                .controller(controller.getClass().getSimpleName()).annotation(((Class) annotation).getSimpleName())
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
