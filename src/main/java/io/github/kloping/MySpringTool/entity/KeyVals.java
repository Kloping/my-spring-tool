package io.github.kloping.MySpringTool.entity;

import org.jsoup.helper.HttpConnection;

import java.util.Collection;

/**
 * @author github.kloping
 */
public interface KeyVals {
    /**
     * @return
     */
    Collection<HttpConnection.KeyVal> values();
}
