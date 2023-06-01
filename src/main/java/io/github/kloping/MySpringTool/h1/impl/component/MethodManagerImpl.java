package io.github.kloping.MySpringTool.h1.impl.component;

import io.github.kloping.MySpringTool.StarterApplication;
import io.github.kloping.MySpringTool.annotations.AutoStandAfter;
import io.github.kloping.MySpringTool.annotations.Bean;
import io.github.kloping.MySpringTool.annotations.CommentScan;
import io.github.kloping.MySpringTool.annotations.Entity;
import io.github.kloping.MySpringTool.interfaces.AutomaticWiringParams;
import io.github.kloping.MySpringTool.interfaces.component.ClassManager;
import io.github.kloping.MySpringTool.interfaces.component.ContextManager;
import io.github.kloping.MySpringTool.interfaces.component.MethodManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class MethodManagerImpl implements MethodManager {
    private AutomaticWiringParams automaticWiringParams;

    public MethodManagerImpl(AutomaticWiringParams automaticWiringParams, ClassManager classManager) {
        this.automaticWiringParams = automaticWiringParams;
        classManager.registeredAnnotation(Entity.class, this);
        classManager.registeredAnnotation(CommentScan.class, this);
    }

    @Override
    public void manager(Method method, ContextManager contextManager) throws IllegalAccessException, InvocationTargetException {
        method.setAccessible(true);
        if (method.isAnnotationPresent(Bean.class)) {
            Class cla = method.getDeclaringClass();
            Object o = contextManager.getContextEntity(cla);
            Object[] objects = automaticWiringParams.wiring(method, contextManager);
            Object ro = method.invoke(o, objects);
            String id = method.getDeclaredAnnotation(Bean.class).value();
            contextManager.append(ro, id);
            StarterApplication.logger.Log("new bean  " + method.getName() + " from " + method.getDeclaringClass().getSimpleName(), 0);
        }
    }

    @Override
    public void manager(Class clas, ContextManager contextManager) throws IllegalAccessException, InvocationTargetException {
        for (Method method : clas.getDeclaredMethods()) {
            this.manager(method, contextManager);
        }
    }
}
