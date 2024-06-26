package io.github.kloping.spt.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
    String value() default "0";
    String type() default "0";
}
