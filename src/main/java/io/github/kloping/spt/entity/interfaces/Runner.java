package io.github.kloping.spt.entity.interfaces;

import io.github.kloping.spt.exceptions.NoRunException;

import java.lang.reflect.Method;

public interface Runner {
    void run(Method method, Object t, Object[] objects) throws NoRunException;
}
