package io.github.kloping.spt.annotations.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * path value
 * @author github-kloping
 */
@Target(ElementType.PARAMETER)
@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
public @interface PathValue {
    String value() default "";
}
