package io.github.kloping.MySpringTool.interfaces.component;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * 类字段管理器
 */
public interface FieldManager extends ClassAttributeManager {
    @Override
    default void manager(AccessibleObject accessibleObject, ContextManager contextManager) throws IllegalAccessException {
        if (accessibleObject instanceof Method)
            manager((Field) accessibleObject, contextManager);
    }

    void manager(Field field, ContextManager contextManager) throws IllegalAccessException;

    void manager(Class clsz, ContextManager contextManager) throws IllegalAccessException;
}

