package io.github.kloping.MySpringTool.annotations.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * @author github-kloping
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Callback {
    /**
     * on request data call method(s)
     * <p>
     * template
     * Callback("io.github.kloping.spt.M0")
     * this method return type must is String
     * else will call toString
     *
     * @return
     */
    String[] value();
}
