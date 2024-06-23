package io.github.kloping.spt.interfaces;

import java.lang.reflect.Method;

/**
 * 所有执行器 父接口
 */
public interface Executor {
    Object execute(Object this_, Method method, Object... objects) throws Throwable;
}
