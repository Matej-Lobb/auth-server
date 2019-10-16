package sk.mlobb.authserver.model.permission;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
public @interface DefaultPermission {

    boolean readAll() default false;
    boolean readSelf() default false;
    boolean writeAll() default false;
    boolean writeSelf() default false;
}
