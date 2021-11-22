package io.github.kloping.MySpringTool.interfaces;

import io.github.kloping.MySpringTool.interfaces.component.ContextManager;

import java.lang.reflect.Field;

/**
 * 自动布线-属性
 */
public interface AutomaticWiringValue extends AutomaticWiring {
    boolean wiring(Object o, Field field, ContextManager contextManager) throws IllegalAccessException;

    boolean wiring(Field field, ContextManager contextManager) throws IllegalAccessException;
}
