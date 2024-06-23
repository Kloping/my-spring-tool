package io.github.kloping.spt.interfaces.component.up0;

import io.github.kloping.spt.interfaces.component.ContextManager;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;

/**
 * Class Attribute Manager
 *
 * @author github-kloping
 */
public interface ClassAttributeManager extends BaseManager {
    @Override
    default void manager(Object o) throws IllegalAccessException, InvocationTargetException {
    }

    /**
     * manager a accessibleObject
     *
     * @param accessibleObject
     * @param contextManager
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    void manager(AccessibleObject accessibleObject, ContextManager contextManager) throws InvocationTargetException, IllegalAccessException;

    /**
     * manager a class
     *
     * @param clsz
     * @param contextManager
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    void manager(Class clsz, ContextManager contextManager) throws IllegalAccessException, InvocationTargetException;
}
