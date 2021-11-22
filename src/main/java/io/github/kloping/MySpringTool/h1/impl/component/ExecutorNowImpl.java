package io.github.kloping.MySpringTool.h1.impl.component;

import io.github.kloping.MySpringTool.entity.interfaces.Runner;
import io.github.kloping.MySpringTool.interfaces.Executor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ExecutorNowImpl implements Executor {
    @Override
    public Object execute(Object this_, Method method, Object... objects) throws InvocationTargetException, IllegalAccessException {
        Object o = method.invoke(this_, objects);
        return o;
    }

    @Override
    public void setBefore(Runner runner) {}

    @Override
    public void setAfter(Runner runner) {}
}
