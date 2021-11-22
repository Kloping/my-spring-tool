package io.github.kloping.MySpringTool.entity.interfaces;

import io.github.kloping.MySpringTool.exceptions.NoRunException;

public abstract interface Runner {
    public abstract void run(Object t, Object[] objects) throws NoRunException;
}
