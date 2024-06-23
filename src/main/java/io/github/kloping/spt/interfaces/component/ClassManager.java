package io.github.kloping.spt.interfaces.component;

import io.github.kloping.spt.interfaces.component.up0.BaseManager;
import io.github.kloping.spt.interfaces.component.up0.ClassAttributeManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

/**
 * 类管理器
 */
public interface ClassManager extends BaseManager {
    @Override
    default void manager(Object o)  {
        if (o instanceof Class<?>)
            add((Class<?>) o);
    }

    <T extends Annotation> void registeredAnnotation(Class<T> annotation, ClassAttributeManager attributeManager);

    void add(Class<?> cla);
}
