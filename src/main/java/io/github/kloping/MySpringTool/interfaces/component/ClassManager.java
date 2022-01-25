package io.github.kloping.MySpringTool.interfaces.component;

import io.github.kloping.MySpringTool.interfaces.component.up0.BaseManager;
import io.github.kloping.MySpringTool.interfaces.component.up0.ClassAttributeManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;

/**
 * 类管理器
 */
public interface ClassManager extends BaseManager {
    @Override
    default void manager(Object o) throws InvocationTargetException, InstantiationException, IllegalAccessException {
        if (o instanceof Class<?>)
            add((Class<?>) o);
    }

    <T extends Annotation> void registeredAnnotation(Class<T> annotation, ClassAttributeManager attributeManager);

    void add(Class<?> cla) throws InvocationTargetException, InstantiationException, IllegalAccessException;
}
