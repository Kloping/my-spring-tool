package io.github.kloping.MySpringTool.h1.impl.component;

import io.github.kloping.MySpringTool.entity.interfaces.Runner;
import io.github.kloping.MySpringTool.interfaces.Executor;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ExecutorImpl implements Executor {
    private int size = 20;

    private long waitTime = 5 * 1000L;

    private java.util.concurrent.ExecutorService threads;
    private Runner runner1;
    private Runner runner2;

    @Override
    public void setBefore(Runner runner) {
        runner1 = runner;
    }

    @Override
    public void setAfter(Runner runner) {
        runner2 = runner;
    }

    private void init() {
        threads = new ThreadPoolExecutor(size, size, waitTime, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(size));
    }

    public ExecutorImpl(int size, long waitTime) {
        this.size = size;
        this.waitTime = waitTime;
        init();

    }

    public ExecutorImpl(long waitTime) {
        this.waitTime = waitTime;
        init();
    }


    @Override
    public Future execute(Object this_, Method method, Object... objects) throws InvocationTargetException, IllegalAccessException {
        threads.submit(() -> {
            try {
                if (runner1 != null)
                    runner1.run(method, this_, objects);
                Object o = method.invoke(this_, objects);
                if (runner2 != null)
                    runner2.run(method, this_, objects);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            }
        });
        return null;
    }
}
