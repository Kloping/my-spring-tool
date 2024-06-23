package io.github.kloping.spt.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 注解在{@link Entity}的构造方法时将指定其为构造 不使用其他 构造方法
 * @author github.kloping
 */
@Target(ElementType.CONSTRUCTOR)
@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
public @interface Constructor {
    /**
     * 创建失败处理方式
     * <br> 0:抛出任意{@link RuntimeException} 继续执行下一构造方法
     * <br> 1:抛出任意{@link RuntimeException} 结束创建
     *
     * @return
     */
    int value() default 0;
}