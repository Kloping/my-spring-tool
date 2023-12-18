package io.github.kloping.MySpringTool.h1.impl.component;

import io.github.kloping.MySpringTool.entity.interfaces.Runner;
import io.github.kloping.MySpringTool.entity.interfaces.RunnerOnThrows;
import io.github.kloping.MySpringTool.interfaces.Executor;

import java.lang.reflect.Method;

/**
 * @author github-kloping
 */
public class ExecutorNowImpl implements Executor {
    @Override
    public Object execute(Object this_, Method method, Object... objects) throws Throwable {
        Object o = method.invoke(this_, objects);
        return o;
    }
}
