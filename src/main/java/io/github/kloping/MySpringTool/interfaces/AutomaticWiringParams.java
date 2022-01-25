package io.github.kloping.MySpringTool.interfaces;

import io.github.kloping.MySpringTool.interfaces.component.ContextManager;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

/**
 * Automatic wiring - parameters
 *
 * @author github-kloping
 */
public interface AutomaticWiringParams extends AutomaticWiring {
    /**
     * auto waring use contextManager
     *
     * @param method
     * @param contextManager
     * @return
     * @throws IllegalAccessException
     */
    Object[] wiring(Method method, ContextManager contextManager) throws IllegalAccessException;

    /**
     * auto waring use objects
     *
     * @param method
     * @param objects
     * @return
     * @throws IllegalAccessException
     */
    Object[] wiring(Method method, Object... objects) throws IllegalAccessException;
}
