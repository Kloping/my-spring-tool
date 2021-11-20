package io.github.kloping.MySpringTool.annotations.http;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CookieFrom {
    /**
     * urls
     * on  "this" use will execute url
     *
     * @return
     */
    String[] value();

    /**
     * only GET or POST
     *
     * @return
     */
    String method() default "GET";
}
