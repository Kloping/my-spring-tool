package io.github.kloping.spt.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@java.lang.annotation.Retention(RetentionPolicy.RUNTIME)
public @interface Bean {
    String value() default "0";
}
