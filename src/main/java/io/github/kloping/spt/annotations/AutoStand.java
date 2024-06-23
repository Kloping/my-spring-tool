package io.github.kloping.spt.annotations;

import java.lang.annotation.*;

@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.FIELD)
public @interface AutoStand {
    String id() default "";
}
