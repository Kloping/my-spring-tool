package io.github.kloping.MySpringTool.annotations.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * default value
 * parameter is null use default value
 * only use on {@link io.github.kloping.MySpringTool.annotations.http.ParamName}
 *
 * @author github kloping
 */
@Target(ElementType.PARAMETER)
@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
public @interface DefaultValue {
    String value();
}
