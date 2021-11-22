package io.github.kloping.MySpringTool.interfaces.component;

import java.lang.reflect.InvocationTargetException;

public interface BaseManager {
    void manager(Object o) throws InvocationTargetException, InstantiationException, IllegalAccessException;
}
