package io.github.kloping.MySpringTool.interfaces.component;

import io.github.kloping.MySpringTool.interfaces.component.up0.SourceManager;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

/**
 * This interface is used to manage all fields
 *
 * @author github.kloping
 */
public interface FieldSourceManager extends SourceManager {
    /**
     * Gets all fields of the class
     *
     * @param cla
     * @return
     */
    Field[] getFields(Class<?> cla);

    /**
     * Gets an annotated field from a class
     *
     * @param cla
     * @param annotation
     * @return
     */
    Field[] getFields(Class<?> cla, Annotation annotation);
}
