package io.github.kloping.MySpringTool.interfaces.component;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 类方法管理器
 * @author github-kloping
 */
public interface MethodManager extends ClassAttributeManager {
    /**
     * Manages a {@link AccessibleObject}
     * if it's method goto {@link MethodManager#manager(Method, ContextManager)}
     *
     * @param accessibleObject
     * @param contextManager
     * @throws InvocationTargetException
     * @throws IllegalAccessException
     */
    @Override
    default void manager(AccessibleObject accessibleObject, ContextManager contextManager) throws InvocationTargetException, IllegalAccessException {
        if (accessibleObject instanceof Method){
            manager((Method) accessibleObject, contextManager);
        }
    }

    /**
     * Manage a method with {@link ContextManager}
     *
     * @param method
     * @param contextManager
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    void manager(Method method, ContextManager contextManager) throws IllegalAccessException, InvocationTargetException;

    /**
     * Manages all the methods of a class
     *
     * @param clas
     * @param contextManager
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @Override
    default void manager(Class clas, ContextManager contextManager) throws IllegalAccessException, InvocationTargetException {
        for (Method method : clas.getDeclaredMethods()) {
            method.setAccessible(true);
            this.manager(method, contextManager);
        }
    }
}
