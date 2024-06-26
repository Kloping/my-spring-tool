package io.github.kloping.spt.interfaces;

import io.github.kloping.spt.entity.interfaces.Runner;
import io.github.kloping.spt.entity.interfaces.RunnerOnThrows;

/**
 * 队列执行器
 * @author github-kloping
 */
public interface QueueExecutor extends Executor {

    <T> int queueExecute(T t, Object... objects);

    <T extends Runner> void setBefore(T runner);

    <T extends Runner> void setAfter(T runner);

    <T extends RunnerOnThrows> void setException(T r);
}
