package io.github.kloping.MySpringTool.interfaces.component;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * -@TimeEve
 * -@Schedule
 * 管理
 */
public interface TimeMethodManager extends MethodManager{
    @Override
    default void manager(Object o) throws IllegalAccessException, InvocationTargetException {
        MethodManager.super.manager(o);
    }

    @Override
    default void manager(AccessibleObject accessibleObject, ContextManager contextManager) throws InvocationTargetException, IllegalAccessException {
        MethodManager.super.manager(accessibleObject, contextManager);
    }

    @Override
    void manager(Method method, ContextManager contextManager) throws IllegalAccessException, InvocationTargetException;

    @Override
    void manager(Class clas, ContextManager contextManager) throws IllegalAccessException, InvocationTargetException;
}
