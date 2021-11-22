package io.github.kloping.MySpringTool.interfaces.component;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;

public interface ClassAttributeManager extends BaseManager {

    @Override
    default void manager(Object o)throws IllegalAccessException, InvocationTargetException {}

    void manager(AccessibleObject accessibleObject, ContextManager contextManager) throws InvocationTargetException, IllegalAccessException;

    void manager(Class clsz, ContextManager contextManager) throws IllegalAccessException, InvocationTargetException;
}
