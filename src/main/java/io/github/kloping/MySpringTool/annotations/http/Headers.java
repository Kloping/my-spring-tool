package io.github.kloping.MySpringTool.annotations.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The annotated type should be {@link  java.util.Map<String,String>}
 * Because the object will be converted to the head of the request
 *
 * @author github kloping
 * @version 1.0
 */
@Target(ElementType.PARAMETER)
@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
public @interface Headers {
}
