package io.github.kloping.MySpringTool.entity.interfaces;

/**
 * @author github.kloping
 */
public interface RunnerOnThrows {
    void onThrows(Throwable throwable, Object t, Object... args);
}
