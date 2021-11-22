package io.github.kloping.MySpringTool.interfaces.component;

import java.lang.reflect.InvocationTargetException;

/**
 * 基础 管理器
 */
public interface BaseManager {
    void manager(Object o) throws InvocationTargetException, InstantiationException, IllegalAccessException;
}
