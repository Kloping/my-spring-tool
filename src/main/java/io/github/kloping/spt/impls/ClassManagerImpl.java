package io.github.kloping.spt.impls;

import io.github.kloping.map.MapUtils;
import io.github.kloping.spt.PartUtils;
import io.github.kloping.spt.annotations.ComponentScan;
import io.github.kloping.spt.annotations.Controller;
import io.github.kloping.spt.annotations.Entity;
import io.github.kloping.spt.interfaces.AutomaticWiringParams;
import io.github.kloping.spt.interfaces.Logger;
import io.github.kloping.spt.interfaces.component.ActionManager;
import io.github.kloping.spt.interfaces.component.ClassManager;
import io.github.kloping.spt.interfaces.component.ContextManager;
import io.github.kloping.spt.interfaces.component.InstanceCrater;
import io.github.kloping.spt.interfaces.component.up0.ClassAttributeManager;

import java.lang.annotation.Annotation;
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
    public void add(Class<?> cla) {
        if (!set.add(cla)) return;
        String id = null;
        if (cla.isAnnotationPresent(ComponentScan.class)) {
            Object o = contextManager.getContextEntity(cla);
        }
        if (cla.isAnnotationPresent(Entity.class)) {
            Object o = contextManager.getContextEntity(cla);
            if (o == null) {
                id = cla.getDeclaredAnnotation(Entity.class).value();
                createBeanByIdAndCla(id, cla);
            }
        }
        if (cla.isAnnotationPresent(Controller.class)) {
            Object o = contextManager.getContextEntity(cla);
            if (o == null) {
                id = cla.getDeclaredAnnotation(Controller.class).value();
                o = createBeanByIdAndCla(id, cla);
            }
        }
        for (Class<? extends Annotation> annotationClass : registeredAnnotations.keySet()) {
            if (cla.isAnnotationPresent(annotationClass)) {
                for (ClassAttributeManager classAttributeManager : registeredAnnotations.get(annotationClass)) {
                    try {
                        classAttributeManager.manager(cla, contextManager);
                    } catch (Exception e) {
                        Logger logger = contextManager.getContextEntity(Logger.class);
                        if (logger != null) logger.error(PartUtils.getExceptionLine(e));
                        else e.printStackTrace();
                    }
                }
            }
        }
    }

    private Object createBeanByIdAndCla(String id, Class cla) {
        Object o = instanceCrater.create(cla, contextManager);
        if (o != null) contextManager.append(o, id);
        return o;
    }
}
