package io.github.kloping.MySpringTool.entity.impls;

import io.github.kloping.MySpringTool.entity.interfaces.Runner;

import java.lang.reflect.Method;

public abstract class RunnerEve implements Runner {
    public abstract void methodRuined(Object ret,Method method, Object t, Object... objects);
}
