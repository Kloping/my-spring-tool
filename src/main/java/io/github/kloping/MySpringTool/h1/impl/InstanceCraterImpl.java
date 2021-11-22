package io.github.kloping.MySpringTool.h1.impl;

import io.github.kloping.MySpringTool.interfaces.component.ContextManager;
import io.github.kloping.MySpringTool.interfaces.component.InstanceCrater;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;

public class InstanceCraterImpl implements InstanceCrater {
    @Override
    public <T> T create(Class<T> cla, ContextManager contextManager) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        Constructor constructor = null;
        if ((constructor = m1(cla)) != null) {
            constructor.setAccessible(true);
            return (T) constructor.newInstance();
        }
        Constructor<?>[] constructors = cla.getDeclaredConstructors();
        for (Constructor cons : constructors) {
            cons.setAccessible(true);
            if (m2(contextManager, cons.getParameters())) {
                Parameter[] parameters = cons.getParameters();
                Object[] objects = new Object[parameters.length];
                for (int i = 0; i < parameters.length; i++) {
                    objects[i] = contextManager.getContextEntity(parameters[i].getType());
                }
                return (T) cons.newInstance(objects);
            }
        }
        return null;
    }

    private boolean m2(ContextManager contextManager, Parameter[] parameters) {
        for (Parameter parameter : parameters) {
            if (contextManager.getContextEntity(parameter.getType()) != null) continue;
            else return false;
        }
        return true;
    }

    private Constructor m1(Class<?> cla) {
        Constructor[] constructors = cla.getDeclaredConstructors();
        for (Constructor constructor : constructors) {
            if (constructor.getParameters().length == 0) return constructor;
        }
        return null;
    }
}
