package io.github.kloping.MySpringTool.h1.impl.component;

import io.github.kloping.MySpringTool.interfaces.component.HttpClientManager;
import org.jsoup.Connection;
import org.jsoup.nodes.Document;

import java.lang.reflect.Method;

/**
 * @author github.kloping
 */
public interface HttpStatusReceiver {
    /**
     * @param manager 请求
     * @param url
     * @param code       http请求返回码
     * @param interface0 被代理的类
     * @param method     被代理的方法
     * @param reqMethod  请求方法
     * @param cla        返回的类型
     * @param o          返回的实例
     * @param metadata   元数据
     */
    void receive(HttpClientManager manager,String url, Integer code, Class<?> interface0, Method method,
                 Connection.Method reqMethod,
                 Class<?> cla, Object o, Document metadata);
}
