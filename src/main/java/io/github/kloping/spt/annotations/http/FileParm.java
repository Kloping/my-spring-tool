package io.github.kloping.spt.annotations.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * multipart/form-data
 * file
 * 被注解的参数应为byte
 *
 * @author github.kloping
 */
@Target(ElementType.PARAMETER)
@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
public @interface FileParm {
    /**
     * field name
     *
     * @return
     */
    String value();

    /**
     * file name
     *
     * @return
     */
    String name();

    String type() default "";
}
