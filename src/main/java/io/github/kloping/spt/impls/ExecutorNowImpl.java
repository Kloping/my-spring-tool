package io.github.kloping.spt.impls;

import io.github.kloping.spt.interfaces.Executor;

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
