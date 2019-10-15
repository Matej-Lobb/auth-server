package sk.mlobb.authserver.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class PermissionTableService {

    private final ApplicationContext applicationContext;

    private Object[] acceptedAnnotations = {GetMapping.class, PostMapping.class, DeleteMapping.class,
            PutMapping.class, PatchMapping.class};

    public PermissionTableService(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void getAllControllers() {
        Map<Method, Class<? extends Annotation>> controlledMethods = new HashMap<>();

        Map<String, Object> beansWithAnnotation = applicationContext.getBeansWithAnnotation(RestController.class);
        for (Object controller : beansWithAnnotation.values()) {
            Method[] declaredMethods = controller.getClass().getDeclaredMethods();
            for (Method method : declaredMethods) {
                for (Annotation annotation : method.getDeclaredAnnotations()) {
                    for (Object object : acceptedAnnotations) {
                        if (annotation.annotationType().equals(object))
                            controlledMethods.put(method, annotation.annotationType());
                    }
                }
            }
        }

        System.out.println(controlledMethods);
    }

    static class Permission {
        private String methodName;
    }
}
