package io.github.kloping.MySpringTool.interfaces.component.up0;

import java.lang.reflect.InvocationTargetException;

/**
 * base manager
 *
 * @author github-kloping
 */
public interface BaseManager {
    /**
     * manager a object
     *
     * @param o
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    void manager(Object o) throws InvocationTargetException, InstantiationException, IllegalAccessException;
}
