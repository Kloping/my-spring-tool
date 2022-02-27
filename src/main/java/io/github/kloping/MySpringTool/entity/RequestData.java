package io.github.kloping.MySpringTool.entity;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

/**
 * @author github.kloping
 */
public interface RequestData {
    /**
     * get entry set will use to {@link org.jsoup.Connection#data(String, String)}
     *
     * @return
     */
    default Set<Entry<String, String>> getEntrySet() {
        return getDataMap().entrySet();
    }

    /**
     * data Map
     *
     * @return
     */
    Map<String, String> getDataMap();
}
