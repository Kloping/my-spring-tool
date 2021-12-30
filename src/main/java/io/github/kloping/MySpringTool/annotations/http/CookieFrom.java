package io.github.kloping.MySpringTool.annotations.http;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Request with cookie from value
 * The default request for obtaining cookies is GET
 * @author github-kloping
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CookieFrom {
    String method() default "GET";
    String[] value();
}
