
package io.github.kloping.spt.impls.baseup;

import io.github.kloping.spt.Setting;
import io.github.kloping.spt.entity.impls.RunnerEve;
import io.github.kloping.spt.entity.interfaces.Runner;
import io.github.kloping.spt.entity.interfaces.RunnerOnThrows;
import io.github.kloping.spt.impls.QueueExecutorImpl;
import io.github.kloping.spt.interfaces.Executor;
import io.github.kloping.spt.interfaces.QueueExecutor;
import io.github.kloping.spt.interfaces.entitys.MatherResult;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

import static io.github.kloping.spt.PartUtils.getExceptionLine;

/**
 * @author github-kloping
 */
@Slf4j
public class QueueExecutorWithReturnsImpl extends QueueExecutorImpl implements QueueExecutor {
    protected Class<?> cla = Long.class;
    protected Executor executor;
    protected int poolSize = 20;
    protected long waitTime = 10 * 1000;
    private RunnerEve runner1;
    private RunnerEve runner2;
    private RunnerOnThrows onThrows;

    @Override
    public <T extends RunnerOnThrows> void setException(T r) {
        this.onThrows = r;
    }

    @Override
    public <T extends Runner> void setBefore(T runner) {
        if (runner instanceof RunnerEve)
            runner1 = (RunnerEve) runner;
    }

    @Override
    public <T extends Runner> void setAfter(T runner) {
        if (runner instanceof RunnerEve)
            runner2 = (RunnerEve) runner;
    }

    public QueueExecutorWithReturnsImpl(Class<?> cla, Executor executor, Setting setting) {
        super(setting);
        this.cla = cla;
        this.executor = executor;
        init();
    }

    private ExecutorService threads;
    private ExecutorService runThreads = null;
    protected Map<Object, Queue> queueMap = new ConcurrentHashMap<>();

    protected Set<Object> runSet = new CopyOnWriteArraySet<>();


    protected void init() {
        threads = new ThreadPoolExecutor(poolSize, poolSize, waitTime, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(poolSize));
        runThreads = Executors.newFixedThreadPool(poolSize);
    }

    public void shutdown() {
        if (threads != null) threads.shutdownNow();
        if (runThreads != null) runThreads.shutdownNow();
    }

    protected QueueExecutorWithReturnsImpl(Class<?> cla, int poolSize, long waitTime, Executor executor, Setting setting) {
        super(setting);
        this.executor = executor;
        this.poolSize = poolSize;
        this.cla = cla;
        this.waitTime = waitTime;
        this.init();
    }

    @Override
    public Object execute(Object This, Method method, Object... objects) throws Throwable {
        return executor.execute(This, method, objects);
    }

    @Override
    public <T> int queueExecute(T t, Object... objects) {
        if (t == null || objects == null || objects.length < 2) {
            log.warn("queueExecute requires a key and action");
            return -1;
        }
        if (t.getClass() != cla) {
            log.warn("not is mainKey type for {}", t.getClass().getSimpleName());
            return -1;
        } else {
            if (runSet.add(t)) {
                tryRun(t, objects);
                return queueMap.size();
            } else {
                append(t, objects);
                log.debug("append queue list and next run");
            }
        }
        return 0;
    }

    protected <T> void tryRun(T t, Object[] objects) {
        runThreads.execute(() -> {
            Future future = threads.submit(() -> {
                long startTime = System.currentTimeMillis();
                Object[] parts = Arrays.copyOfRange(objects, 2, objects.length);
                if (setting.getArgsManager().isLegal(parts)) {
                    matcherAndRun(t, objects, startTime, (Object) parts);
                } else {
                    log.warn("Can't Access types for {}", Arrays.toString(objects));
                }
            });
            getVal(future);
            runEnd(t);
        });
    }

    protected <T> void matcherAndRun(T t, Object[] objects, long startTime, Object parts) {
        try {
            MatherResult result = setting.getActionManager().mather(objects[1].toString());
            if (result != null && result.getMethods().length > 0) {
                Method[] methods = result.getMethods();
                Class cla = methods[0].getDeclaringClass();
                Object o = setting.getContextManager().getContextEntity(cla);
                List<Object> results = new ArrayList<>();
                for (Method m : methods) {
                    Object[] parObjs = setting.getAutomaticWiringParams().wiring(m, result, results, (Object) parts);
                    if (runner1 != null) {
                        runner1.methodRuined(null, m, t, objects);
                    }
                    Object o1 = executor.execute(o, m, parObjs);
                    if (o1 != null) {
                        results.add(o1);
                        if (runner2 != null) {
                            runner2.methodRuined(o1, m, t, objects);
                        }
                    }
                }
                log.debug("lost time {} Millisecond", System.currentTimeMillis() - startTime);
            } else log.warn("No match for {}", objects[1]);
        } catch (Throwable e) {
            if (onThrows != null) {
                onThrows.onThrows(e, t, objects);
            } else log.error(getExceptionLine(e), e);
        }
    }

    protected <T> void runEnd(T t) {
        runSet.remove(t);
        Object[] next = end(t);
        if (next != null) {
            queueExecute(t, next);
        }
    }

    protected void getVal(Future future) {
        try {
            future.get(waitTime, TimeUnit.MILLISECONDS);
        } catch (InterruptedException e) {
            log.error("运行时错误(Running Has Error)", e);
            future.cancel(true);
        } catch (TimeoutException e) {
            log.error("运行超时(Run Time Out)", e);
            future.cancel(true);
        } catch (Exception e) {
            log.error("其他错误(Other Error)", e);
            future.cancel(true);
        }
    }

    protected void append(Object t, Object... objects) {
        if (queueMap.containsKey(t)) {
            queueMap.get(t).offer(objects);
        } else {
            Queue queue = new ConcurrentLinkedDeque();
            queue.offer(objects);
            queueMap.put(t, queue);
        }
    }

    private Object[] end(Object t) {
        Queue queue = queueMap.get(t);
        if (queue == null) return null;
        Object[] objects = (Object[]) queue.poll();
        if (queue.isEmpty()) queueMap.remove(t, queue);
        return objects;
    }
}
