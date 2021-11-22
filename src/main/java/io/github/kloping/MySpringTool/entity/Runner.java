package io.github.kloping.MySpringTool.entity;

import io.github.kloping.MySpringTool.exceptions.NoRunException;

public abstract class Runner {
    public enum state {
        BEFORE, AFTER
    }

    private state state;
    private Object t;
    private Object[] objects;

    public Runner(Runner.state state) {
        this.state = state;
    }

    public abstract void run(Object t, Object[] objects) throws NoRunException;

    public Runner.state getState() {
        return state;
    }
}
