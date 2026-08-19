package io.github.kloping.spt.impls;

import io.github.kloping.spt.Setting;
import io.github.kloping.spt.entity.interfaces.Runner;
import io.github.kloping.spt.entity.interfaces.RunnerOnThrows;
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
public class QueueExecutorImpl extends ExecutorNowImpl implements QueueExecutor {
    private Class<?> cla = Long.class;
    private int poolSize = 20;
    private long waitTime = 10 * 1000;
    private Runner runner1;
    private Runner runner2;
    private RunnerOnThrows onThrows;
    protected Executor executor;
    protected Setting setting;

    private java.util.concurrent.ExecutorService threads;
    private ExecutorService runThreads = null;

    public QueueExecutorImpl(Setting setting) {
        this.setting = setting;
    }

    @Override
    public <T extends Runner> void setBefore(T runner) {
        this.runner1 = runner;
    }

    @Override
    public <T extends Runner> void setAfter(T runner) {
        this.runner2 = runner;
    }

    @Override
    public <T extends RunnerOnThrows> void setException(T r) {
        this.onThrows = r;
    }

    private void init() {
        threads = new ThreadPoolExecutor(poolSize, poolSize, waitTime, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(poolSize));
        runThreads = Executors.newFixedThreadPool(poolSize);
    }

    public static QueueExecutor create(Class<?> cla, int poolSize, long waitTime, Executor executor, Setting setting) {
        QueueExecutorImpl queueExecutor = new QueueExecutorImpl(setting);
        queueExecutor.executor = executor;
        queueExecutor.poolSize = poolSize;
        queueExecutor.cla = cla;
        queueExecutor.waitTime = waitTime;
        queueExecutor.init();
        return queueExecutor;
    }

    private Map<Object, Queue> queueMap = new ConcurrentHashMap<>();

    private Set<Object> runSet = new CopyOnWriteArraySet<>();

    @Override
    public <T> int queueExecute(T t, Object... objects) {
        if (t == null || objects == null || objects.length < 2) {
            log.warn("queueExecute requires a key and action");
            return -1;
        }
        if (t.getClass() != cla) {
            log.warn("not is mainKey type for {}", t.getClass().getSimpleName());
            return 0;
        } else {
            if (runSet.add(t)) {
                runThreads.execute(() -> {
                    Future future = threads.submit(() -> {
                        long startTime = System.currentTimeMillis();
                        Object[] parts = Arrays.copyOfRange(objects, 2, objects.length);
                        if (setting.getArgsManager().isLegal(parts)) {
                            try {
                                MatherResult result = setting.getActionManager().mather(objects[1].toString());
                                if (result != null && result.getMethods().length > 0) {
                                    Method[] methods = result.getMethods();
                                    Class cla = methods[0].getDeclaringClass();
                                    if (runner1 != null) runner1.run(methods[0], t, objects);
                                    Object o = setting.getContextManager().getContextEntity(cla);
                                    Object reo = null;
                                    List<Object> results = new ArrayList<>();
                                    for (Method m : methods) {
                                        Object[] parObjs = setting.getAutomaticWiringParams().wiring(m, result, results, (Object) parts);
                                        Object to = this.execute(o, m, parObjs);
                                        if (to != null) {
                                            results.add(to);
                                            reo = to;
                                        }
                                    }
                                    if (runner2 != null) runner2.run(methods[0], reo, objects);
                                    log.debug("lost time {} Millisecond", System.currentTimeMillis() - startTime);
                                } else log.warn("No match for {}", objects[1]);
                            } catch (Throwable e) {
                                if (onThrows != null) {
                                    onThrows.onThrows(e, t, objects);
                                } else log.error(getExceptionLine(e), e);
                            }
                        } else {
                            log.warn("Can't Access types for {}", Arrays.toString(objects));
                        }
                    });

                    try {
                        future.get(waitTime, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        log.error("Running Has Error", e);
                        future.cancel(true);
                    } catch (TimeoutException e) {
                        log.error("Run Time Out", e);
                        future.cancel(true);
                    } catch (Exception e) {
                        log.error("Other Error", e);
                        future.cancel(true);
                    }
                    runSet.remove(t);
                    Object[] next = end(t);
                    if (next != null) {
                        queueExecute(t, next);
                    }
                });
                return queueMap.size();
            } else {
                append(t, objects);
                log.debug("append queue list and next run");
            }
        }
        return 0;
    }

    private void append(Object t, Object... objects) {
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

    public void shutdown() {
        if (threads != null) threads.shutdownNow();
        if (runThreads != null) runThreads.shutdownNow();
    }
}
