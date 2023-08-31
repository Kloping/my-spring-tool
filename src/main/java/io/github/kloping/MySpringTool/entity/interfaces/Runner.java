package io.github.kloping.MySpringTool.entity.interfaces;

import io.github.kloping.MySpringTool.exceptions.NoRunException;

import java.lang.reflect.Method;

public interface Runner {
    void run(Method method, Object t, Object[] objects) throws NoRunException;
}
