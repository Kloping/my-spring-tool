package io.github.kloping.spt.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 当前entity 所有字段 autoStand后运行
 */
@Target(ElementType.METHOD)
@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
public @interface AutoStandAfter {
}
