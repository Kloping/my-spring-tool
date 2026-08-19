package io.github.kloping.spt.impls;

import io.github.kloping.spt.annotations.Bean;
import io.github.kloping.spt.annotations.ComponentScan;
import io.github.kloping.spt.annotations.Entity;
import io.github.kloping.spt.interfaces.AutomaticWiringParams;
import lombok.extern.slf4j.Slf4j;
import io.github.kloping.spt.interfaces.component.ClassManager;
import io.github.kloping.spt.interfaces.component.ContextManager;
import io.github.kloping.spt.interfaces.component.MethodManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@Slf4j
public class MethodManagerImpl implements MethodManager {
    private AutomaticWiringParams automaticWiringParams;

    public MethodManagerImpl(AutomaticWiringParams automaticWiringParams, ClassManager classManager) {
        this.automaticWiringParams = automaticWiringParams;
        classManager.registeredAnnotation(Entity.class, this);
        classManager.registeredAnnotation(ComponentScan.class, this);
    }

    @Override
    public void manager(Method method, ContextManager contextManager) throws IllegalAccessException, InvocationTargetException {
        method.setAccessible(true);
        if (method.isAnnotationPresent(Bean.class)) {
            Class cla = method.getDeclaringClass();
            Object o = contextManager.getContextEntity(cla);
            Object[] objects = automaticWiringParams.wiring(method, contextManager);
            if (o == null || objects == null) {
                throw new IllegalStateException("Unable to wire @Bean method: " + method);
            }
            Object ro = method.invoke(o, objects);
            if (ro == null) return;
            String id = method.getDeclaredAnnotation(Bean.class).value();
            contextManager.append(ro, id);
            log.debug("new bean {} from {}", method.getName(), method.getDeclaringClass().getSimpleName());
        }
    }

    @Override
    public void manager(Class clas, ContextManager contextManager) throws IllegalAccessException, InvocationTargetException {
        for (Method method : clas.getDeclaredMethods()) {
            this.manager(method, contextManager);
        }
    }
}
