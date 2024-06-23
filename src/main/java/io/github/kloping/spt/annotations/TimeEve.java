package io.github.kloping.spt.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
/**
 * 仅用于静态方法
 * 每一段时间触发一次该静态方法
 * 格式为 单位:毫秒
 */
public @interface TimeEve {
    long value() default -1L;
}
