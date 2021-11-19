package io.github.kloping.MySpringTool.entity;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * Param 放在 @HttpClient 接口里 作为 可变长 参数
 */
public class Params {
    protected Map<String, String> params = new LinkedHashMap<>();

    Params(Map<String, String> params) {
        this.params = params;
    }

    public Map<String, String> getParams() {
        return params;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        params.forEach((k, v) -> {
            sb.append(k).append("=").append(v)
                    .append("&");
        });
        String ss = sb.toString();
        ss = ss.substring(0, ss.length() - 1);
        return ss;
    }
}
