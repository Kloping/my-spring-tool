package io.github.kloping.spt.interfaces.component;

import java.lang.reflect.InvocationTargetException;

/**
 * 实例创建者
 */
public interface InstanceCrater {
    <T> T create(Class<T> cla, ContextManager contextManager);
}
