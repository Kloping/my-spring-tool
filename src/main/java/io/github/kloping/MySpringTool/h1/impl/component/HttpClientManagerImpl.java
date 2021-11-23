package io.github.kloping.MySpringTool.h1.impl.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.github.kloping.MySpringTool.StarterApplication;
import io.github.kloping.MySpringTool.annotations.http.*;
import io.github.kloping.MySpringTool.entity.Params;
import io.github.kloping.MySpringTool.interfaces.component.ClassManager;
import io.github.kloping.MySpringTool.interfaces.component.ContextManager;
import io.github.kloping.MySpringTool.interfaces.component.HttpClientManager;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.reflect.*;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.kloping.MySpringTool.partUtils.getExceptionLine;

public class HttpClientManagerImpl implements HttpClientManager {
    private abstract class M1<T> {
        private String path;

        private M1(String path) {
            this.path = path;
        }

        abstract T run(Object... objects);
    }

    public HttpClientManagerImpl(ClassManager classManager) {
        classManager.registeredAnnotation(HttpClient.class, this);
    }

    @Override
    public void manager(Method method, ContextManager contextManager) throws IllegalAccessException, InvocationTargetException {
        method.setAccessible(true);
        String host = method.getDeclaringClass().getDeclaredAnnotation(HttpClient.class).value();
        if (method.isAnnotationPresent(GetPath.class)) {
            String path = method.getAnnotation(GetPath.class).value();
            path = ali(host, path);
            InitMethod(method, path, 0);
        } else if (method.isAnnotationPresent(PostPath.class)) {
            String path = method.getAnnotation(PostPath.class).value();
            path = ali(host, path);
            InitMethod(method, path, 1);
        }
    }

    private Map<Method, M1> methodInks = new ConcurrentHashMap<>();

    @Override
    public void manager(Class cla, ContextManager contextManager) throws IllegalAccessException, InvocationTargetException {
        contextManager.append(cla, Proxy.newProxyInstance(cla.getClassLoader(),
                new Class[]{cla}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (methodInks.containsKey(method))
                            return methodInks.get(method).run(args);
                        return null;
                    }
                }), UUID.randomUUID().toString());
        for (Method declaredMethod : cla.getDeclaredMethods()) {
            this.manager(declaredMethod, contextManager);
        }
    }

    private String ali(String host, String path) {
        if (!host.endsWith("/")) host += "/";
        if (path.startsWith("/")) path = path.substring(1, path.length());
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        return host + path;
    }

    private void InitMethod(Method method, String url, int type) {
        if (type == 0) {
            methodInks.put(method, new M1(url) {
                @Override
                Object run(Object... objects) {
                    try {
                        String trueUrl = getGetUrl(url, method, objects);
                        Connection connection = getConnection(trueUrl);
                        initCookie(connection, method, trueUrl);
                        Class<?> cls = method.getReturnType();
                        if (cls == String.class)
                            return connection.get().toString();
                        else if (cls == Document.class)
                            return connection.get();
                        if (cls == byte[].class)
                            return connection.method(Connection.Method.GET).execute().bodyAsBytes();
                        return Type(cls, connection.get().body().text());
                    } catch (Exception e) {
                        StarterApplication.logger.Log(getExceptionLine(e), -1);
                    }
                    return null;
                }
            });
        } else if (type == 1) {
            methodInks.put(method, new M1(url) {
                @Override
                Object run(Object... objects) {
                    try {
                        String trueUrl = getGetUrl(url, method, objects);
                        String body = getPostBody(method, objects);
                        Connection connection = getConnection(trueUrl).requestBody(body);
                        initCookie(connection, method, trueUrl);
                        Class<?> cls = method.getReturnType();
                        if (cls == String.class)
                            return connection.post().toString();
                        else if (cls == Document.class)
                            return connection.post();
                        if (cls == byte[].class)
                            return connection.method(Connection.Method.POST).execute().bodyAsBytes();
                        return Type(cls, connection.post().body().text());
                    } catch (Exception e) {
                        StarterApplication.logger.Log(e.getMessage() + getExceptionLine(e), -1);
                    }
                    return null;
                }
            });
        }
    }

    private void initCookie(Connection connection, Method method, String trueUrl) throws Exception {
        if (method.isAnnotationPresent(CookieFrom.class)) {
            CookieFrom cf = method.getAnnotation(CookieFrom.class);
            connection.cookieStore(getCookieStore(cf.value(), Connection.Method.valueOf(cf.method()), trueUrl));
        }
    }

    public String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36 Edg/95.0.1020.53";

    private Connection getConnection(String trueUrl) throws Exception {
        return Jsoup.connect(trueUrl).ignoreContentType(true)
                .userAgent(userAgent);
    }

    private String getPostBody(Method method, Object[] objects) throws Exception {
        Parameter[] parameters = method.getParameters();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(RequestBody.class)) {
                RequestBody rb = parameters[i].getAnnotation(RequestBody.class);
                switch (rb.type()) {
                    case toString:
                        sb.append(objects[i]);
                        break;
                    case json:
                        sb.append(JSON.toJSONString(objects[i]));
                        break;
                }
            }
        }
        return sb.toString();
    }

    private <T> T Type(Class<T> cls, String text) {
        if (cls == byte[].class) return (T) text.getBytes();
        return JSON.parseObject(text).toJavaObject(cls);
    }

    public Map<String, CookieStore> histCookie = new ConcurrentHashMap<>();
    public Map<String, URI> histURI = new ConcurrentHashMap<>();

    private CookieStore getCookieStore(String[] urls, Connection.Method method, String url) throws IOException, URISyntaxException {
        CookieStore store = null;
        for (String u1 : urls) {
            try {
                Connection connection = null;
                CookieStore sc1 = null;
                Document document = null;
                URI uri = null;
                if (u1.trim().toLowerCase().equals("this")) {
                    u1 = url.trim();
                }
                if (histCookie.containsKey(u1)) {
                    sc1 = histCookie.get(u1);
                    uri = histURI.get(u1);
                } else {
                    connection = getConnection(u1);
                    if (method == Connection.Method.GET) {
                        document = connection.get();
                    } else if (method == Connection.Method.POST) {
                        document = connection.post();
                    }
                    sc1 = connection.cookieStore();
                    uri = new URI(document.baseUri());
                    histCookie.put(u1, sc1);
                    histURI.put(u1, uri);
                }
                if (store == null)
                    store = sc1;
                else {
                    for (HttpCookie httpCookie : sc1.getCookies()) {
                        store.add(uri, httpCookie);
                    }
                }
            } catch (Exception e) {
                StarterApplication.logger.Log("get Cookie Failed From: " + u1, 2);
                continue;
            }
        }
        return store;
    }

    private String getGetUrl(String url, Method method, Object... objects) {
        Parameter[] parameters = method.getParameters();
        StringBuilder sb = new StringBuilder();
        sb.append(url);
        sb.append("?");
        for (int i = 0; i < parameters.length; i++) {
            if (objects[i] instanceof Params) {
                Params params = (Params) objects[i];
                params.getParams().forEach((k, v) -> {
                    sb.append(k).append("=").append(v).append("&");
                });
            } else if (parameters[i].isAnnotationPresent(ParamName.class)) {
                ParamName pn = parameters[i].getAnnotation(ParamName.class);
                String k = pn.value();
                sb.append(k).append("=").append(objects[i].toString()).append("&");
            } else if (parameters[i].isAnnotationPresent(ParamBody.class)) {
                ParamBody pn = parameters[i].getAnnotation(ParamBody.class);
                JSONObject jo = JSON.parseObject(JSON.toJSONString(objects[i]));
                jo.forEach((k, v) -> {
                    sb.append(k).append("=").append(v).append("&");
                });
            }
        }
        if (sb.toString().endsWith("?")) sb.delete(sb.length() - 1, sb.length());
        if (sb.toString().endsWith("&")) sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }
}
