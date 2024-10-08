package io.github.kloping.spt.interfaces;

import io.github.kloping.spt.interfaces.component.ContextManager;

import java.lang.reflect.Field;

/**
 * Automatic wiring - Field
 *
 * @author github-kloping
 */
public interface AutomaticWiringValue extends AutomaticWiring {
    /**
     * on object fill instance use contextManager
     *
     * @param o
     * @param field
     * @param contextManager
     * @return
     * @throws IllegalAccessException
     */
    boolean wiring(Object o, Field field, ContextManager contextManager) throws IllegalAccessException;

    /**
     * fill instance use contextManager
     *
     * @param field
     * @param contextManager
     * @return
     * @throws IllegalAccessException
     */
    boolean wiring(Field field, ContextManager contextManager) throws IllegalAccessException;

    /**
     * fill instance use contextManager
     *
     * @param obj
     * @param contextManager
     * @return
     * @throws IllegalAccessException
     */
    boolean wiring(Object obj, ContextManager contextManager) throws IllegalAccessException;
}
