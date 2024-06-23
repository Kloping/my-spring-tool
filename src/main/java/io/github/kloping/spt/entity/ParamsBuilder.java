package io.github.kloping.spt.entity;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 构建一个 Param 对象
 */
public class ParamsBuilder {
    private Map<String, String> params = new LinkedHashMap<>();

    public ParamsBuilder append(String name, String value) {
        params.put(name, value);
        return this;
    }

    public ParamsBuilder remove(String name) {
        params.remove(name);
        return this;
    }

    public Params build() {
        return new Params(params);
    }
}
