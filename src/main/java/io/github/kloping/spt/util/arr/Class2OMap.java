package io.github.kloping.spt.util.arr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Class2OMap {
    private final Map<Class<?>, List<Object>> values = new HashMap<>();
    private boolean identical;
    private boolean memory = true;

    public static <T> Class2OMap create(T... objects) {
        Class2OMap result = new Class2OMap();
        if (objects != null) for (Object object : objects) if (object != null) {
            result.values.computeIfAbsent(object.getClass(), k -> new ArrayList<>()).add(object);
        }
        return result;
    }

    public <T> T get(Class<T> type) { return get(type, 0); }

    public <T> T get(Class<T> type, int index) {
        if (type == null || index < 0) return null;
        List<Object> list = getList(type);
        return index < list.size() ? type.cast(list.get(index)) : null;
    }

    public Integer getSize(Class<?> type) { return getList(type).size(); }

    public <T, T1> List<T> getList(Class<T1> type) {
        List<T> result = new ArrayList<>();
        if (type == null) return result;
        values.forEach((actual, list) -> {
            if ((identical && actual == type) || (!identical && type.isAssignableFrom(actual))) {
                for (Object value : list) result.add((T) value);
            }
        });
        return result;
    }

    public boolean isIdentical() { return identical; }
    public void setIdentical(boolean identical) { this.identical = identical; }
    public boolean isMemory() { return memory; }
    public void setMemory(boolean memory) { this.memory = memory; }
}
