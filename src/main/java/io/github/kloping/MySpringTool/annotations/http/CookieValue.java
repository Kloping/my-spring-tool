package io.github.kloping.MySpringTool.annotations.http;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The parameter type annotated by this annotation should be {@link java.util.Map.Entry}
 *
 * @author github.kloping
 */
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CookieValue {
}
