package io.github.kloping.spt.impls;

import io.github.kloping.spt.annotations.ComponentScan;
import io.github.kloping.spt.annotations.Controller;
import io.github.kloping.spt.annotations.Entity;
import io.github.kloping.spt.interfaces.component.up0.ClassAttributeManager;
import io.github.kloping.spt.interfaces.component.ClassManager;
import io.github.kloping.spt.interfaces.component.ContextManager;
import io.github.kloping.spt.interfaces.component.FieldSourceManager;
import io.github.kloping.map.MapUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author github-kloping
 */
public class FieldSourceManagerImpl0 implements ClassAttributeManager, FieldSourceManager {
    private ClassManager classManager;
    public final Map<Class<?>, Map<Annotation, List<Field>>> FieldMap = new ConcurrentHashMap<>();

    @Override
    public void manager(AccessibleObject accessibleObject, ContextManager contextManager) throws InvocationTargetException, IllegalAccessException {
        if (accessibleObject instanceof Field) {
            Field field = (Field) accessibleObject;
            Class classes = field.getDeclaringClass();
            Map<Annotation, List<Field>> map = FieldMap.get(classes);
            if (map == null) {
                map = new ConcurrentHashMap<>();
            }
            for (Annotation annotation : field.getAnnotations()) {
                MapUtils.append(map, annotation, field);
            }
        }
    }

    @Override
    public void manager(Class classes, ContextManager contextManager) throws IllegalAccessException, InvocationTargetException {
        for (Field declaredField : classes.getDeclaredFields()) {
            Map<Annotation, List<Field>> map = FieldMap.get(classes);
            if (map == null) {
                map = new ConcurrentHashMap<>();
            }
            for (Annotation annotation : declaredField.getAnnotations()) {
                MapUtils.append(map, annotation, declaredField);
            }
            FieldMap.put(classes, map);
        }
    }

    public FieldSourceManagerImpl0(ClassManager classManager) {
        this.classManager = classManager;
        classManager.registeredAnnotation(Entity.class, this);
        classManager.registeredAnnotation(Controller.class, this);
        classManager.registeredAnnotation(ComponentScan.class, this);
    }

    @Override
    public Field[] getFields(Class<?> cla) {
        Map<Annotation, List<Field>> map = FieldMap.get(cla);
        List<Field> list = new CopyOnWriteArrayList<>();
        for (List<Field> value : map.values()) {
            list.addAll(value);
        }
        return list.toArray(new Field[0]);
    }

    @Override
    public Field[] getFields(Class<?> cla, Annotation annotation) {
        Map<Annotation, List<Field>> map = FieldMap.get(cla);
        List<Field> list = new CopyOnWriteArrayList<>();
        map.forEach((k, v) -> {
            if (k.getClass() == annotation.getClass()) {
                list.addAll(v);
            } else if (annotation.getClass().isAssignableFrom(k.getClass())) {
                list.addAll(v);
            }
        });
        return list.toArray(new Field[0]);
    }

    @Override
    public AccessibleObject get(String name) {
        try {
            int last = name.lastIndexOf(".");
            String className = name.substring(0, last);
            String fieldName = name.substring(last + 1, name.length());
            Class<?> cla = Class.forName(className);
            Field field = cla.getField(fieldName);
            return field;
        } catch (ClassNotFoundException | NoSuchFieldException e) {
            e.printStackTrace();
        }
        return null;
    }
}
