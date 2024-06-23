package io.github.kloping.spt.impls;

import io.github.kloping.spt.interfaces.AutomaticWiringParams;
import io.github.kloping.spt.interfaces.component.ContextManager;
import io.github.kloping.object.ObjectUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

/**
 * @author github-kloping
 */
public class AutomaticWiringParamsImpl implements AutomaticWiringParams {
    @Override
    public Object[] wiring(Method method, ContextManager contextManager) throws IllegalAccessException {
        Parameter[] parameters = method.getParameters();
        if (!m2(contextManager, parameters)) return null;
        Object[] objects = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            objects[i] = contextManager.getContextEntity(parameters[i].getType());
        }
        return objects;
    }

    private boolean m2(ContextManager contextManager, Parameter[] parameters) {
        for (Parameter parameter : parameters) {
            if (contextManager.getContextEntity(parameter.getType()) != null) continue;
            else return false;
        }
        return true;
    }

    @Override
    public Object[] wiring(Method method, Object... objects) throws IllegalAccessException {
        Parameter[] parameters = method.getParameters();
        Object[] ros = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            for (Object o : objects) {
                Class pc = ObjectUtils.baseToPack(parameters[i].getType());
                if (ObjectUtils.isSuperOrInterface(o.getClass(), pc)) {
                    ros[i] = o;
                }
            }
        }
        return ros;
    }
}
