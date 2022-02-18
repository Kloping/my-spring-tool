package io.github.kloping.MySpringTool.h1.impl;

import io.github.kloping.MySpringTool.annotations.AutoStand;
import io.github.kloping.MySpringTool.interfaces.AutomaticWiringValue;
import io.github.kloping.MySpringTool.interfaces.component.ContextManager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author github-kloping
 */
public class AutomaticWiringValueImpl implements AutomaticWiringValue {
    @Override
    public boolean wiring(Object o, Field field, ContextManager contextManager) throws IllegalAccessException {
        field.setAccessible(true);
        if (field.isAnnotationPresent(AutoStand.class)) {
            AutoStand autoStand = field.getDeclaredAnnotation(AutoStand.class);
            Object o1 = null;
            if (autoStand.id().equals("")) {
                o1 = contextManager.getContextEntity(field.getType());
            } else o1 = contextManager.getContextEntity(field.getType(), autoStand.id());
            if (o1 != null)
                field.set(o, o1);
            if (Modifier.isStatic(field.getModifiers()))
                field.set(null, o1);
            return true;
        }
        return false;
    }

    @Override
    public boolean wiring(Field field, ContextManager contextManager) throws IllegalAccessException {
        return this.wiring(null, field, contextManager);
    }
}
