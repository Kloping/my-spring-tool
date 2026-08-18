package io.github.kloping.spt.util;

import java.util.LinkedHashMap;
import java.util.Map;

public final class HmlObject {
    private final Map<String, Object> entry = new LinkedHashMap<>();

    public static HmlObject parse(String text) {
        HmlObject result = new HmlObject();
        for (String line : text.split("\\R")) {
            String value = line.trim();
            if (value.isEmpty() || value.startsWith("#")) continue;
            int split = value.indexOf('=');
            if (split > 0) result.entry.put(value.substring(0, split).trim(), ObjectUtils.maybeType(value.substring(split + 1).trim()));
        }
        return result;
    }

    public Map<String, Object> getEntry() { return entry; }
}
