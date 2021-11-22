package io.github.kloping.MySpringTool.interfaces.component;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface MethodManager extends ClassAttributeManager {
    @Override
    default void manager(AccessibleObject accessibleObject, ContextManager contextManager) throws InvocationTargetException, IllegalAccessException {
        if (accessibleObject instanceof Method)
            manager((Method) accessibleObject, contextManager);
    }

    void manager(Method method, ContextManager contextManager) throws IllegalAccessException, InvocationTargetException;

    default void manager(Class clas, ContextManager contextManager) throws IllegalAccessException, InvocationTargetException {
        for (Method method : clas.getDeclaredMethods()) {
            method.setAccessible(true);
            this.manager(method, contextManager);
        }
    }
}
