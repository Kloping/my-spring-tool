package io.github.kloping.MySpringTool.h1.impl.component;

import io.github.kloping.MySpringTool.annotations.CommentScan;
import io.github.kloping.MySpringTool.annotations.Controller;
import io.github.kloping.MySpringTool.annotations.Entity;
import io.github.kloping.MySpringTool.interfaces.AutomaticWiringParams;
import io.github.kloping.MySpringTool.interfaces.component.*;
import io.github.kloping.MySpringTool.interfaces.component.up0.ClassAttributeManager;
import io.github.kloping.map.MapUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author github-kloping
 */
public class ClassManagerImpl implements ClassManager {

    private InstanceCrater instanceCrater;

    private ContextManager contextManager;

    private AutomaticWiringParams automaticWiringParams;

    private ActionManager actionManager;

    public ClassManagerImpl(InstanceCrater instanceCrater, ContextManager contextManager
            , AutomaticWiringParams automaticWiringParams, ActionManager actionManager) {
        this.instanceCrater = instanceCrater;
        this.contextManager = contextManager;
        this.automaticWiringParams = automaticWiringParams;
        this.actionManager = actionManager;
    }

    private Map<Class<? extends Annotation>, List<ClassAttributeManager>> registeredAnnotations = new ConcurrentHashMap<>();

    @Override
    public <T extends Annotation> void registeredAnnotation(Class<T> annotation, ClassAttributeManager attributeManager) {
        MapUtils.append(registeredAnnotations, annotation, attributeManager, CopyOnWriteArrayList.class);
    }

    private Set<Class<?>> set = new CopyOnWriteArraySet<>();

    @Override
    public void add(Class<?> cla) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (!set.add(cla)) return;
        String id = null;
        if (cla.isAnnotationPresent(CommentScan.class)) {
            Object o = contextManager.getContextEntity(cla);
        }
        if (cla.isAnnotationPresent(Entity.class)) {
            Object o = contextManager.getContextEntity(cla);
            if (o == null) {
                id = cla.getDeclaredAnnotation(Entity.class).value();
                m1(id, cla);
            }
        }
        if (cla.isAnnotationPresent(Controller.class)) {
            Object o = contextManager.getContextEntity(cla);
            if (o == null) {
                id = cla.getDeclaredAnnotation(Controller.class).value();
                o = m1(id, cla);
            }
        }
        for (Class<? extends Annotation> annotationClass : registeredAnnotations.keySet()) {
            if (cla.isAnnotationPresent(annotationClass)) {
                for (ClassAttributeManager classAttributeManager : registeredAnnotations.get(annotationClass)) {
                    classAttributeManager.manager(cla, contextManager);
                }
            }
        }
    }

    private Object m1(String id, Class cla) throws InvocationTargetException, IllegalAccessException, InstantiationException {
        Object o = instanceCrater.create(cla, contextManager);
        contextManager.append(o, id);
        return o;
    }
}
