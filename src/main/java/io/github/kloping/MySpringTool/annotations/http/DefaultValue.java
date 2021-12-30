package io.github.kloping.MySpringTool.annotations.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author github kloping
 * @version 1.0
 * @date 2021/12/30-15:29
 */
@Target(ElementType.PARAMETER)
@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
public @interface DefaultValue {
    /**
     * default value
     * parameter is null use default value
     * only use on {@link io.github.kloping.MySpringTool.annotations.http.ParamName}
     *
     * @return
     */
    String value();
}
