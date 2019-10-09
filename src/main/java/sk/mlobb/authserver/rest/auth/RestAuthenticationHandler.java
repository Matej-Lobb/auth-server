package sk.mlobb.authserver.rest.auth;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.aop.support.AopUtils;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import sk.mlobb.authserver.model.Role;
import sk.mlobb.authserver.model.User;
import sk.mlobb.authserver.model.exception.NotFoundException;
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

    /**
     * Check access with Reflections
     *
     * This will only work if file Name contains 'Controller' in name
     */
    public void checkAccess() {
        final Method[] methods = AopUtils.getTargetClass(getClassName()).getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equalsIgnoreCase("method") && method.isAnnotationPresent(Secured.class)) {
                boolean authenticated = false;
                for (Role role : getUserFromContext().getRoles()) {
                    for (String annotationValue : method.getAnnotation(Secured.class).value()) {
                        if (role.getRole().equalsIgnoreCase(annotationValue)) {
                            authenticated = true;
                            break;
                        }
                    }
                }
                if (!authenticated) {
                    throw new UsernameNotFoundException("No access to resource !");
                }
            }
        }
    }

    public boolean isAdminAccess() {
        for (Role role : getUserFromContext().getRoles()) {
            if (Boolean.TRUE.equals(role.getSuperAccess())) {
                return true;
            }
        }
        return false;
    }

    public User getUserFromContext() {
        try {
            return userService.getUserByName(SecurityContextHolder.getContext().getAuthentication()
                    .getPrincipal().toString());
        } catch (NotFoundException e) {
            throw new UsernameNotFoundException("User not exist in current context!");
        }
    }

    private Class<?> getClassName() {
        StackTraceElement controller = getController();
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
