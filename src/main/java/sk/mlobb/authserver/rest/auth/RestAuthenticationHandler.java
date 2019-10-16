package sk.mlobb.authserver.rest.auth;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import sk.mlobb.authserver.model.Role;
import sk.mlobb.authserver.model.User;
import sk.mlobb.authserver.model.exception.NotFoundException;
import sk.mlobb.authserver.model.permission.Permission;
import sk.mlobb.authserver.service.UserService;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class RestAuthenticationHandler {

    private static final String PACKAGE_NAME = "sk.mlobb.authserver";
    private static final String CONTROLLER = "Controller";

    private final UserService userService;

    public RestAuthenticationHandler(UserService userService) {
        this.userService = userService;
    }

    public void checkAccess() {
        Permission permission = getPermission();
        log.debug(permission.toString());
        // TODO Handle Permission
    }

    private Permission getPermission() {
        StackTraceElement controller = getController();
        final Method[] methods = getClassName(controller).getDeclaredMethods();
        Permission finalPermission = null;
        for (Method method : methods) {
            if (method.getName().equalsIgnoreCase(controller.getMethodName())) {
                for (Role role : getUserFromContext().getRoles()) {
                    for (Permission rolePermission : role.getPermissions()) {
                        if (rolePermission.getMethodName().equals(controller.getMethodName())) {
                            finalPermission = rolePermission;
                            break;
                        }
                    }
                }
            }
        }
        if (finalPermission == null) {
            throw new UsernameNotFoundException("No access to resource !");
        }
        return finalPermission;
    }

    private User getUserFromContext() {
        try {
            return userService.getUserByName(SecurityContextHolder.getContext().getAuthentication()
                    .getPrincipal().toString());
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
        throw new UsernameNotFoundException("Failed to find controller file !");
    }

    private StackTraceElement getController() {
        List<StackTraceElement> projectClasses = getProjectClasses();
        for (StackTraceElement stackTraceElement : projectClasses) {
            if (stackTraceElement.getClassName().contains(CONTROLLER)) {
                return stackTraceElement;
            }
        }
        throw new UsernameNotFoundException("Failed to find controller file !");
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
