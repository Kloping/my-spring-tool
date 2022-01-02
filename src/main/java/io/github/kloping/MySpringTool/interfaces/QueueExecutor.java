package io.github.kloping.MySpringTool.interfaces;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 队列执行器
 * @author github-kloping
 */
public interface QueueExecutor extends Executor {
    Object execute(Object this_, Method method, Object... objects) throws InvocationTargetException, IllegalAccessException;

    <T> int QueueExecute(T t, Object... objects);
}
