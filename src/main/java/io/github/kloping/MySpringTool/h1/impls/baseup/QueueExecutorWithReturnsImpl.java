
package io.github.kloping.MySpringTool.h1.impls.baseup;

import io.github.kloping.MySpringTool.Setting;
import io.github.kloping.MySpringTool.entity.impls.RunnerEve;
import io.github.kloping.MySpringTool.entity.interfaces.Runner;
import io.github.kloping.MySpringTool.exceptions.NoRunException;
import io.github.kloping.MySpringTool.interfaces.Executor;
import io.github.kloping.MySpringTool.interfaces.QueueExecutor;
import io.github.kloping.MySpringTool.interfaces.entitys.MatherResult;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;

import static io.github.kloping.MySpringTool.StarterApplication.logger;
import static io.github.kloping.MySpringTool.PartUtils.getExceptionLine;

/**
 * @author github-kloping
 */
public class QueueExecutorWithReturnsImpl implements QueueExecutor {
    protected Class<?> cla = Long.class;
    protected Executor executor;
    protected int poolSize = 20;
    protected long waitTime = 10 * 1000;
    private RunnerEve runner1;
    private RunnerEve runner2;
    private Setting setting;

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
        this.setting = setting;
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

    protected QueueExecutorWithReturnsImpl(Class<?> cla, int poolSize, long waitTime, Executor executor, Setting setting) {
        this.setting = setting;
        this.executor = executor;
        this.poolSize = poolSize;
        this.cla = cla;
        this.waitTime = waitTime;
        this.init();
    }

    public static QueueExecutorWithReturnsImpl create(Class<?> cla, int poolSize, long waitTime, Executor executor, Setting setting) {
        return new QueueExecutorWithReturnsImpl(cla, poolSize, waitTime, executor, setting);
    }

    @Override
    public Object execute(Object This, Method method, Object... objects) throws InvocationTargetException, IllegalAccessException {
        Object o = null;
        try {
            o = executor.execute(This, method, objects);
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

    @Override
    public <T> int queueExecute(T t, Object... objects) {
        if (t.getClass() != cla) {
            logger.Log("not is mainKey type for " + t.getClass().getSimpleName(), 2);
            return -1;
        } else {
            if (runSet.add(t)) {
                tryRun(t, objects);
                return queueMap.size();
            } else {
                append(t, objects);
                logger.Log("append queue list and next run", 0);
            }
        }
        return 0;
    }

    protected <T> void tryRun(T t, Object[] objects) {
        runThreads.execute(() -> {
            Future future = threads.submit(() -> {
                try {
                    long startTime = System.currentTimeMillis();
                    Object[] parts = Arrays.copyOfRange(objects, 2, objects.length);
                    if (setting.getArgsManager().isLegal(parts)) {
                        matcherAndRun(t, objects, startTime, (Object) parts);
                    } else {
                        logger.Log("Can't Access types for " + Arrays.toString(objects), 2);
                    }
                } catch (NoRunException e) {
                    logger.Log("抛出 不运行异常(throw NuRunException): " + e.getMessage() + " At " + getExceptionLine(e), 2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            getVal(future);
            runEnd(t);
        });
    }

    protected <T> void matcherAndRun(T t, Object[] objects, long startTime, Object parts) {
        try {
            MatherResult result = setting.getActionManager().mather(objects[1].toString());
            if (result != null) {
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
    }

    protected <T> void runEnd(T t) {
        runSet.remove(t);
        if (queueMap.containsKey(t)) {
            queueExecute(t, end(t));
        }
    }

    protected void getVal(Future future) {
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
        Object[] objects = null;
        if (queueMap.containsKey(t)) {
            objects = (Object[]) queueMap.get(t).poll();
        }
        if (queueMap.get(t).isEmpty())
            queueMap.remove(t);
        return objects;
    }
}
