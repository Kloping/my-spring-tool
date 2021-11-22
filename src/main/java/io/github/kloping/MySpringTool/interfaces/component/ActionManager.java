package io.github.kloping.MySpringTool.interfaces.component;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;

public interface ActionManager extends MethodManager {
    @Override
    default void manager(AccessibleObject accessibleObject, ContextManager contextManager) throws InvocationTargetException, IllegalAccessException {
    }

    @Override
    default void manager(Class clsz, ContextManager contextManager) throws IllegalAccessException, InvocationTargetException {
        manager(contextManager.getContextEntity(clsz));
    }

    <T> T mather(String regx);

    Class<?>[] getAll();

    void manager(Object obj) throws IllegalAccessException, InvocationTargetException;
}
