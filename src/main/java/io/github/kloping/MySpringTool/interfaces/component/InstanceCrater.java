package io.github.kloping.MySpringTool.interfaces.component;

import java.lang.reflect.InvocationTargetException;

public interface InstanceCrater {
    <T> T create(Class<T> cla, ContextManager contextManager) throws InvocationTargetException, InstantiationException, IllegalAccessException;
}
