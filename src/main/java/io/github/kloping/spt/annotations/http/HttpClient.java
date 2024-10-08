package io.github.kloping.spt.annotations.http;

import io.github.kloping.spt.PartUtils;
import org.jsoup.Connection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * http  客户端
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface HttpClient {
    String value();
}
