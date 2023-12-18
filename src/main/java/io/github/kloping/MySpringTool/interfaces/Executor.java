package io.github.kloping.MySpringTool.interfaces;

import io.github.kloping.MySpringTool.entity.interfaces.Runner;
import io.github.kloping.MySpringTool.entity.interfaces.RunnerOnThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 所有执行器 父接口
 */
public interface Executor {
    Object execute(Object this_, Method method, Object... objects) throws Throwable;
}
