
package io.github.kloping.MySpringTool.h1.impls.baseup;

import io.github.kloping.MySpringTool.interfaces.Executor;
import io.github.kloping.MySpringTool.interfaces.component.Callback;
import io.github.kloping.MySpringTool.interfaces.component.Filter;
import io.github.kloping.MySpringTool.interfaces.component.Interceptor;
import io.github.kloping.MySpringTool.interfaces.component.InterceptorCallback;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.kloping.MySpringTool.StarterApplication.logger;

/**
 * @author github-kloping
 */
public class QueueExecutorWithReturnsAndInterceptorImpl
        extends QueueExecutorWithReturnsImpl
        implements Interceptor, InterceptorCallback {

    public QueueExecutorWithReturnsAndInterceptorImpl(Class<?> cla, Executor executor) {
        super(cla, executor);
    }

    protected QueueExecutorWithReturnsAndInterceptorImpl(Class<?> cla, int poolSize, long waitTime, Executor executor) {
        super(cla, poolSize, waitTime, executor);
    }

    public static QueueExecutorWithReturnsAndInterceptorImpl create(Class<?> cla, int poolSize, long waitTime, Executor executor) {
        return new QueueExecutorWithReturnsAndInterceptorImpl(cla, poolSize, waitTime, executor);
    }

    private Map<Filter, Integer> interceptMap = new ConcurrentHashMap<>();
    private Map<Filter, Callback> interceptCall = new ConcurrentHashMap<>();

    @Override
    public int addIntercept(int c, Callback callback, Filter filter) {
        interceptMap.put(filter, c);
        interceptCall.put(filter, callback);
        return interceptCall.size();
    }

    @Override
    public int addIntercept(int c, Filter filter) {
        interceptMap.put(filter, c);
        return interceptMap.size();
    }

    @Override
    public synchronized boolean intercept(Object... objects) {
        if (!interceptMap.isEmpty()) {
            for (Filter k : interceptMap.keySet()) {
                if (k.filter(objects)) {
                    int c = interceptMap.get(k).intValue();
                    c--;
                    if (interceptCall.containsKey(k))
                        interceptCall.get(k).call(objects);
                    if (c <= 0) {
                        interceptMap.remove(k);
                        if (interceptCall.containsKey(k)) interceptCall.remove(k);
                    } else interceptMap.put(k, c);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public <T> int queueExecute(T t, Object... objects) {
        if (t.getClass() != cla) {
            logger.Log("not is mainKey type for " + t.getClass().getSimpleName(), 2);
            return 0;
        } else {
            if (runSet.add(t)) {
                if (!intercept(objects))
                    tryRun(t, objects);
                return queueMap.size();
            } else {
                append(t, objects);
                logger.Log("append queue list and next run", 0);
            }
        }
        return 0;
    }
}
