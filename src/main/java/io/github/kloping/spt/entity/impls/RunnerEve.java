package io.github.kloping.spt.entity.impls;

import io.github.kloping.spt.entity.interfaces.Runner;

import java.lang.reflect.Method;

public abstract class RunnerEve implements Runner {
    public abstract void methodRuined(Object ret,Method method, Object t, Object... objects);
}
