package sk.mlobb.authserver.rest.auth;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import sk.mlobb.authserver.model.Role;
import sk.mlobb.authserver.model.User;
import sk.mlobb.authserver.model.enums.RequiredAccess;
import sk.mlobb.authserver.model.exception.NotFoundException;
import sk.mlobb.authserver.model.exception.UnauthorizedException;
import sk.mlobb.authserver.model.permission.Access;
import sk.mlobb.authserver.model.permission.Permission;
import sk.mlobb.authserver.service.RoleService;
import sk.mlobb.authserver.service.UserService;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AuthorizationHandler {

    private static final String PACKAGE_NAME = "sk.mlobb.authserver";
    private static final String CONTROLLER = "Controller";

    private final UserService userService;
    private final RoleService roleService;

    public AuthorizationHandler(UserService userService, RoleService roleService) {
        this.roleService = roleService;
        this.userService = userService;
    }

    @Transactional
    public void validateAccess(RequiredAccess ... requiredAccesses) {
        Access access = getPermission().getAccess();
        for (RequiredAccess requiredAccess : requiredAccesses) {
            switch (requiredAccess) {
                case READ_ALL:
                    checkBoolean(access.isReadAll());
                    break;
                case READ_SELF:
                    checkBoolean(access.isReadSelf());
                    break;
                case WRITE_ALL:
                    checkBoolean(access.isWriteAll());
                    break;
                case WRITE_SELF:
                    checkBoolean(access.isWriteSelf());
                    break;
                default:
                    throw new UnauthorizedException("Unauthorized !");
            }
        }
    }

    @Transactional
    public boolean checkIfAccessingOwnApplicationData(String uid) {
        try {
            userService.checkIfUserIsPartOfApplication(uid, getUserFromContext().getUsername());
            return true;
        } catch (NotFoundException exception) {
            throw exception;
        } catch (Exception exception) {
            return false;
        }
    }

    @Transactional
    public boolean checkIfAccessingOwnUserData(String identifier) {
        if (getUserFromContext().getUsername().equals(identifier)) {
            return true;
        } else {
            return getUserFromContext().getEmail().equals(identifier);
        }
    }

    private void checkBoolean(boolean haveAccess) {
        if (haveAccess) {
            return;
        }
        throw new UnauthorizedException("Unauthorized !");
    }

    private Permission getPermission() {
        StackTraceElement controller = getController();
        final Method[] methods = getClassName(controller).getDeclaredMethods();
        Permission finalPermission = null;
        for (Method method : methods) {
            finalPermission = getControllerMethod(controller, finalPermission, method);
        }
        if (finalPermission == null) {
            finalPermission = buildAndUpdateUserPermission(controller);
        }
        return finalPermission;
    }

    private Permission buildAndUpdateUserPermission(StackTraceElement controllerElement) {
        try {
            String methodName = controllerElement.getMethodName();
            String controllerName = getClassName(controllerElement).getSimpleName();
            log.warn("Permission for {}:{} not found ! Trying to generate new default one!",
                    controllerName, methodName);
            Permission defaultPermission = roleService.getDefaultPermission(controllerName, methodName);
            log.debug("Successfully generated new Permission: {}", defaultPermission);
            //TODO Update user with missing permission
            return defaultPermission;
        } catch (Exception e) {
            log.error("Failed to build permission !", e);
            throw new UnauthorizedException("Unauthorized !");
        }
    }

    private Permission getControllerMethod(StackTraceElement controller, Permission finalPermission, Method method) {
        if (method.getName().equalsIgnoreCase(controller.getMethodName())) {
            finalPermission = getUserPermissions(controller, finalPermission);
        }
        return finalPermission;
    }

    private Permission getUserPermissions(StackTraceElement controller, Permission finalPermission) {
        for (Role role : getUserFromContext().getRoles()) {
            finalPermission = getRolePermissions(controller, finalPermission, role);
        }
        return finalPermission;
    }

    private Permission getRolePermissions(StackTraceElement controller, Permission finalPermission, Role role) {
        for (Permission rolePermission : role.getPermissions()) {
            if (rolePermission.getMethodName().equals(controller.getMethodName())) {
                finalPermission = rolePermission;
                break;
            }
        }
        return finalPermission;
    }

    private User getUserFromContext() {
        try {
            String username = SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString();
            log.debug("Context username: {}", username);
            return userService.getUserByName(username);
        } catch (NotFoundException e) {
            throw new UsernameNotFoundException("User not exist in current context!");
        }
    }

    private Class<?> getClassName(StackTraceElement controller) {
        if (! StringUtils.isEmpty(controller.getFileName())) {
            try {
                return Class.forName(controller.getClassName());
            } catch (ClassNotFoundException e) {
                log.error("Unable to find controller !", e);
            }
        }
        throw new UsernameNotFoundException("Failed to find controller !");
    }

    private StackTraceElement getController() {
        List<StackTraceElement> projectClasses = getProjectClasses();
        for (StackTraceElement stackTraceElement : projectClasses) {
            if (stackTraceElement.getClassName().contains(CONTROLLER)) {
                return stackTraceElement;
            }
        }
        throw new UsernameNotFoundException("Failed to find controller !");
    }

    private List<StackTraceElement> getProjectClasses() {
        List<StackTraceElement> projectsElements = new ArrayList<>();
        for (StackTraceElement stackTraceElement : Thread.currentThread().getStackTrace()) {
            if (stackTraceElement.getFileName() != null && stackTraceElement.getClassName().contains(PACKAGE_NAME)) {
                projectsElements.add(stackTraceElement);
            }
        }
        if (projectsElements.isEmpty()) {
            throw new UsernameNotFoundException("Failed to find package files !");
        }
        return projectsElements;
    }
}
