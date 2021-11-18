package io.github.kloping.MySpringTool.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
/**
 * 仅用于静态方法
 * 一天的某点某时候某秒某分执行该静态方法
 * 格式为24格式
 * 13:13:13
 * 表示为 13点13分13秒
 *  , (逗号 以分割多个时间)
 */
public @interface Schedule {
    String value() default "-1";
}
