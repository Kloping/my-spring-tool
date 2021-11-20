package io.github.kloping.MySpringTool.annotations.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 将这个参数 以 指定名字 作为
 *  k=v& 的参数
 *  k = value
 *  v = o.toString
 */
@Target(ElementType.PARAMETER)
@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
public @interface ParamName {
    String value();
}
