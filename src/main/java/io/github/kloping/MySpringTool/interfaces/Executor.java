package io.github.kloping.MySpringTool.interfaces;

import io.github.kloping.MySpringTool.entity.Runner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public interface Executor {

    void setBefore(Runner runner);

    Object execute(Object this_, Method method, Object... objects) throws InvocationTargetException, IllegalAccessException;

    void setAfter(Runner runner);
}
