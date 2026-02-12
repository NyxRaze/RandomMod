package art.ameliah.fabric.autosprintfix.core.module;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a module class for automatic registration.
 * Classes with this annotation will be automatically discovered and registered
 * by the ModuleManager.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface AutoRegister {

    /**
     * Priority for registration order (lower = registered first).
     * Default is 0.
     * 
     * @return The priority value
     */
    int priority() default 0;
}