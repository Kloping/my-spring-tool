package io.github.kloping.MySpringTool.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface AutoStand {
    String id() default "4002";
}
