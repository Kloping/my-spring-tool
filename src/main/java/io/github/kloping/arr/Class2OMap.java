package io.github.kloping.arr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class Class2OMap {
    private final Map<Class<?>, List<Object>> values = new HashMap<>();

    public static <T> Class2OMap create(T... objects) {
        Class2OMap result = new Class2OMap();
        if (objects != null) for (Object object : objects) if (object != null) {
            result.values.computeIfAbsent(object.getClass(), k -> new ArrayList<>()).add(object);
        }
        return result;
    }

    public <T> T get(Class<T> type) { return get(type, 0); }

    public <T> T get(Class<T> type, int index) {
        List<Object> list = getList(type);
        return index < list.size() ? type.cast(list.get(index)) : null;
    }

    public Integer getSize(Class<?> type) { return getList(type).size(); }

    public <T, T1> List<T> getList(Class<T1> type) {
        List<T> result = new ArrayList<>();
        values.forEach((actual, list) -> { if (type.isAssignableFrom(actual)) for (Object value : list) result.add((T) value); });
        return result;
    }

    public boolean isIdentical() { return false; }
    public void setIdentical(boolean ignored) { }
    public boolean isMemory() { return true; }
    public void setMemory(boolean ignored) { }
}
