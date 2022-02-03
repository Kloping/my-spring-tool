package io.github.kloping.MySpringTool.annotations.http;


import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 将该类内的属性 转为
 * k=v  的形式作为get参数
 *
 * @author github-kloping
 */
@Target(ElementType.PARAMETER)
@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
public @interface ParamBody {
}
