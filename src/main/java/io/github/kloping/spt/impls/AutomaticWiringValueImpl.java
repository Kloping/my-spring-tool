package io.github.kloping.spt.impls;

import io.github.kloping.spt.annotations.AutoStand;
import io.github.kloping.spt.interfaces.AutomaticWiringValue;
import io.github.kloping.spt.interfaces.component.ContextManager;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * @author github-kloping
 */
public class AutomaticWiringValueImpl implements AutomaticWiringValue {
    @Override
    public boolean wiring(Object o, Field field, ContextManager contextManager) throws IllegalAccessException {
        if (o == null) return false;
        field.setAccessible(true);
        if (field.isAnnotationPresent(AutoStand.class)) {
            AutoStand autoStand = field.getDeclaredAnnotation(AutoStand.class);
            Object o1 = null;
            if ("".equals(autoStand.id())) {
                o1 = contextManager.getContextEntity(field.getType());
            } else o1 = contextManager.getContextEntity(field.getType(), autoStand.id());

            if (o1 != null) field.set(o, o1);

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

    @Override
    public boolean wiring(Object obj, ContextManager contextManager) throws IllegalAccessException {
        boolean k = false;
        for (Field declaredField : obj.getClass().getDeclaredFields()) {
            boolean a = wiring(obj, declaredField, contextManager);
            k = a ? a : k;
        }
        return k;
    }
}
