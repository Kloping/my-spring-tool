package io.github.kloping.spt.annotations;


import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author github.kloping
 */
@Target(ElementType.PARAMETER)
@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
public @interface Parameter {
    /**
     * 非action场景指定为id
     *
     * @return
     */
    String value();
}
