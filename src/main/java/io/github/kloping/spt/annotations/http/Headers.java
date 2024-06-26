package io.github.kloping.spt.annotations.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Map;

/**
 * The annotated type should be {@link Map} K is String and V is String
 * Because the object will be converted to the head of the request
 * <p>
 * A field should be specified if the annotation is on the class
 * Such as io.github.kloping.Example.Map
 *
 * @author github kloping
 * @version 1.0
 */
@Target({ElementType.PARAMETER, ElementType.TYPE})
@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
public @interface Headers {
    String value() default "";
}
