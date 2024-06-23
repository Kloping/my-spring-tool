package io.github.kloping.spt.impls;

import io.github.kloping.spt.Setting;
import io.github.kloping.spt.entity.interfaces.Runner;
import io.github.kloping.spt.entity.interfaces.RunnerOnThrows;
import io.github.kloping.spt.interfaces.Executor;
import io.github.kloping.spt.interfaces.Logger;
import io.github.kloping.spt.interfaces.QueueExecutor;
import io.github.kloping.spt.interfaces.entitys.MatherResult;

import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

import static io.github.kloping.spt.PartUtils.getExceptionLine;

/**
 * @author github-kloping
 */
public class QueueExecutorImpl extends ExecutorNowImpl implements QueueExecutor {
    private Class<?> cla = Long.class;
    private int poolSize = 20;
    private long waitTime = 10 * 1000;
    private Runner runner1;
    private Runner runner2;
    private RunnerOnThrows onThrows;
    protected Executor executor;
    protected Setting setting;
    protected Logger logger;

    private java.util.concurrent.ExecutorService threads;
    private ExecutorService runThreads = null;

    public QueueExecutorImpl(Setting setting) {
        this.setting = setting;
        setting.getSTARTED_RUNNABLE().add(() -> {
            logger = setting.getContextManager().getContextEntity(Logger.class);
        });
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

    public static QueueExecutor create(Class<?> cla, int poolSize, long waitTime, Executor executor,Setting setting) {
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
        if (t.getClass() != cla) {
            logger.Log("not is mainKey type for " + t.getClass().getSimpleName(), 2);
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
                                    if (result != null) {
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
                                        logger.Log("lost time " + (System.currentTimeMillis() - startTime) + " Millisecond", 1);
                                    } else logger.Log("No match for " + objects[1].toString(), 2);
                                } catch (Throwable e) {
                                    if (onThrows != null) {
                                        onThrows.onThrows(e, t, objects);
                                    } else logger.error(getExceptionLine(e));
                                }
                            } else {
                                logger.waring("Can't Access types for " + Arrays.toString(objects));
                            }
                    });

                    try {
                        future.get(waitTime, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        logger.Log("Running Has Error\n" + getExceptionLine(e), -1);
                        future.cancel(true);
                    } catch (TimeoutException e) {
                        logger.Log("Run Time Out\n" + getExceptionLine(e), -1);
                        future.cancel(true);
                    } catch (Exception e) {
                        logger.Log("Other Error\n" + getExceptionLine(e), -1);
                        e.printStackTrace();
                        future.cancel(true);
                    }
                    runSet.remove(t);
                    if (queueMap.containsKey(t)) {
                        queueExecute(t, end(t));
                    }
                });
                return queueMap.size();
            } else {
                append(t, objects);
                logger.Log("append queue list and next run", 0);
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
        Object[] objects = null;
        if (queueMap.containsKey(t)) {
            objects = (Object[]) queueMap.get(t).poll();
        }
        if (queueMap.get(t).isEmpty())
            queueMap.remove(t);
        return objects;
    }
}
