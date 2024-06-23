package io.github.kloping.spt.interfaces.entitys;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

public class MatherResult {
    private String actionStr;
    private String regx;
    private Map<String, String> params = new LinkedHashMap<>();
    private Method[] methods;

    public MatherResult(String actionStr, String regx, Method... methods) {
        this.actionStr = actionStr;
        this.regx = regx;
        this.methods = methods;
    }

    public String getActionStr() {
        return actionStr;
    }

    public String getRegx() {
        return regx;
    }

    public Map<String, String> getParams() {
        return params;
    }

    public Method[] getMethods() {
        return methods;
    }
}
