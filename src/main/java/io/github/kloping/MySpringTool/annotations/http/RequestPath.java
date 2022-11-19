package io.github.kloping.MySpringTool.annotations.http;

import org.jsoup.Connection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 其他 方式请路径
 *
 * @author github-kloping
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RequestPath {
    String value();

    Connection.Method method() default Connection.Method.GET;
}
