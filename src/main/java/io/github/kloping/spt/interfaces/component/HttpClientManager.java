package io.github.kloping.spt.interfaces.component;

import io.github.kloping.spt.impls.HttpStatusReceiver;

import java.lang.reflect.InvocationHandler;

/**
 * -@HttpClient
 * 的网络管理
 */
public interface HttpClientManager extends MethodManager, InvocationHandler {
    /**
     * @param receiver
     */
    void addHttpStatusReceiver(HttpStatusReceiver receiver);

    /**
     * 是否打印非错误信息
     * @param k
     */
    void setPrint(boolean k);
}
