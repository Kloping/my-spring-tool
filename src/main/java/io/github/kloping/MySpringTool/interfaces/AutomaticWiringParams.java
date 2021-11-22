package io.github.kloping.MySpringTool.interfaces;

import io.github.kloping.MySpringTool.interfaces.component.ContextManager;

import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Method;

public interface AutomaticWiringParams extends AutomaticWiring {
    Object[] wiring(Method method, ContextManager contextManager) throws IllegalAccessException;

    Object[] wiring(Method method, Object... objects) throws IllegalAccessException;
}
