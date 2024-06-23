package io.github.kloping.spt.interfaces.component;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.InvocationTargetException;

/**
 * Action 管理器
 *
 * @author github-kloping
 */
public interface ActionManager extends MethodManager {
    @Override
    default void manager(AccessibleObject accessibleObject, ContextManager contextManager) throws InvocationTargetException, IllegalAccessException {
    }

    @Override
    default void manager(Class clsz, ContextManager contextManager) throws IllegalAccessException, InvocationTargetException {
        manager(contextManager.getContextEntity(clsz));
    }

    /**
     * mather a somebody
     *
     * @param regx
     * @param <T>
     * @return
     */
    <T> T mather(String regx);

    /**
     * get all Managed classes
     *
     * @return
     */
    Class<?>[] getAll();

    /**
     * manage A class of obj
     *
     * @param obj
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     */
    @Override
    void manager(Object obj) throws IllegalAccessException, InvocationTargetException;

    /**
     * Dynamically replace @Action's matching primary key character
     *
     * @param oStr   The character to be replaced
     * @param nowStr The character to be replaced with
     */
    void replaceAction(String oStr, String nowStr);
}
