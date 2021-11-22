
package io.github.kloping.MySpringTool.h1.impls.baseup;

import io.github.kloping.MySpringTool.entity.impls.RunnerEve;
import io.github.kloping.MySpringTool.entity.interfaces.Runner;
import io.github.kloping.MySpringTool.exceptions.NoRunException;
import io.github.kloping.MySpringTool.interfaces.Executor;
import io.github.kloping.MySpringTool.interfaces.QueueExecutor;
import io.github.kloping.MySpringTool.interfaces.entitys.MatherResult;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.*;

import static io.github.kloping.MySpringTool.StarterApplication.Setting.INSTANCE;
import static io.github.kloping.MySpringTool.StarterApplication.logger;
import static io.github.kloping.MySpringTool.partUtils.getExceptionLine;

public class QueueExecutorWithReturnsImpl implements QueueExecutor {
    private Class<?> cla = Long.class;
    private Executor executor;
    private int poolSize = 20;
    private long waitTime = 10 * 1000;
    private RunnerEve runner1;
    private RunnerEve runner2;

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

    public QueueExecutorWithReturnsImpl(Class<?> cla, Executor executor) {
        this.cla = cla;
        this.executor = executor;
        init();
    }

    private ExecutorService threads;
    private ExecutorService runThreads = null;

    private void init() {
        threads = new ThreadPoolExecutor(poolSize, poolSize, waitTime, TimeUnit.MILLISECONDS, new ArrayBlockingQueue<>(poolSize));
        runThreads = Executors.newFixedThreadPool(poolSize);
    }

    private QueueExecutorWithReturnsImpl() {
    }

    public static QueueExecutorWithReturnsImpl create(Class<?> cla, int poolSize, long waitTime, Executor executor) {
        QueueExecutorWithReturnsImpl queueExecutor = new QueueExecutorWithReturnsImpl();
        queueExecutor.executor = executor;
        queueExecutor.poolSize = poolSize;
        queueExecutor.cla = cla;
        queueExecutor.waitTime = waitTime;
        queueExecutor.init();
        return queueExecutor;
    }

    @Override
    public Object execute(Object this_, Method method, Object... objects) throws InvocationTargetException, IllegalAccessException {
        Object o = null;
        try {
            o = executor.execute(this_, method, objects);
        } catch (InvocationTargetException e) {
            InvocationTargetException ite = e;
            if (ite.getTargetException().getClass() == NoRunException.class) {
                NoRunException exception = (NoRunException) ite.getTargetException();
                logger.Log("抛出 不运行异常(throw NuRunException): " + exception.getMessage(), 2);
            } else {
                logger.Log("存在映射一个异常(Has a Invoke Exception)=>" + ite.getTargetException() + " at " + getExceptionLine(ite.getTargetException()), -1);
            }
        }
        return o;
    }

    private Map<Object, Queue> queueMap = new ConcurrentHashMap<>();

    private Set<Object> runSet = new CopyOnWriteArraySet<>();

    @Override
    public <T> int QueueExecute(T t, Object... objects) {
        if (t.getClass() != cla) {
            logger.Log("not is mainKey type for " + t.getClass().getSimpleName(), 2);
            return 0;
        } else {
            if (runSet.add(t)) {
                runThreads.execute(() -> {
                    Future future = threads.submit(() -> {
                        try {
                            long startTime = System.currentTimeMillis();
                            Object[] parts = Arrays.copyOfRange(objects, 2, objects.length);
                            if (INSTANCE.getArgsManager().isLegal(parts)) {
                                try {
                                    MatherResult result = INSTANCE.getActionManager().mather(objects[1].toString());
                                    if (result != null) {
                                        Method[] methods = result.getMethods();
                                        Class cla = methods[0].getDeclaringClass();
                                        Object o = INSTANCE.getContextManager().getContextEntity(cla);
                                        for (Method m : methods) {
                                            Object[] parObjs = INSTANCE.getAutomaticWiringParams().wiring(m, result, (Object) parts);
                                            if (runner1 != null) {
                                                runner1.methodRuined(null, m, t, objects);
                                            }
                                            Object o1 = executor.execute(o, m, parObjs);
                                            if (runner2 != null) {
                                                runner2.methodRuined(o1, m, t, objects);
                                            }
                                        }
                                        logger.Log("lost time "
                                                + (System.currentTimeMillis() - startTime) + " Millisecond", 1);
                                    } else logger.Log("No match for " + objects[1].toString(), 2);
                                } catch (NoRunException e) {
                                    logger.Log("抛出 不运行异常(throw NuRunException): "
                                            + e.getMessage() + " At " + getExceptionLine(e), 2);
                                } catch (InvocationTargetException e) {
                                    InvocationTargetException ite = e;
                                    if (ite.getTargetException().getClass() == NoRunException.class) {
                                        NoRunException exception = (NoRunException) ite.getTargetException();
                                        logger.Log("抛出 不运行异常(throw NuRunException): " + exception.getMessage(), 2);
                                    } else {
                                        logger.Log("存在映射一个异常(Has a Invoke Exception)=>"
                                                + ite.getTargetException() + " at " + getExceptionLine(ite.getTargetException()), -1);
                                    }
                                } catch (Exception e) {
                                    logger.Log("存在一个异常(Has a Exception)=>"
                                            + e + " at " + getExceptionLine(e), -1);
                                }
                            } else {
                                logger.Log("Can't Access types for " + Arrays.toString(objects), 2);
                            }

                        } catch (NoRunException e) {
                            logger.Log("抛出 不运行异常(throw NuRunException): " + e.getMessage() + " At " + getExceptionLine(e), 2);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                    try {
                        future.get(waitTime, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        logger.Log("运行时错误(Running Has Error)=>" + e, -1);
                        future.cancel(true);
                    } catch (TimeoutException e) {
                        logger.Log("运行超时(Run Time Out)=>" + e, -1);
                        future.cancel(true);
                    } catch (Exception e) {
                        logger.Log("其他错误(Other Error)=>" + e + "\n", -1);
                        e.printStackTrace();
                        future.cancel(true);
                    }
                    runSet.remove(t);
                    if (queueMap.containsKey(t)) {
                        QueueExecute(t, end(t));
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
