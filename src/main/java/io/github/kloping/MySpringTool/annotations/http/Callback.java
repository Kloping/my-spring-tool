package io.github.kloping.MySpringTool.annotations.http;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

/**
 * @author github-kloping
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Callback {
    /**
     * on request data call method(s)
     *
     * template
     *
     * ```
     *
     * @HttpClint("https://xxxx.xxx")
     * interface M0{
     *   @Callback({"io.github.kloping.spt.M0"})
     *   @GetPath("xxxx/xx")
     *   YouType getData(){
     *   }
     * }
     *
     * ```
     *
     * this method return type must is String
     * else will call toString
     * @return
     */
    String[] value();
}
