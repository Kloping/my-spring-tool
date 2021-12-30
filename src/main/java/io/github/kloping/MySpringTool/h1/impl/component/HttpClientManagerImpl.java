package io.github.kloping.MySpringTool.h1.impl.component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.github.kloping.MySpringTool.StarterApplication;
import io.github.kloping.MySpringTool.annotations.PathValue;
import io.github.kloping.MySpringTool.annotations.http.*;
import io.github.kloping.MySpringTool.entity.Params;
import io.github.kloping.MySpringTool.h1.impl.AutomaticWiringParamsImpl;
import io.github.kloping.MySpringTool.interfaces.component.ClassManager;
import io.github.kloping.MySpringTool.interfaces.component.ContextManager;
import io.github.kloping.MySpringTool.interfaces.component.HttpClientManager;
import io.github.kloping.object.ObjectUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.lang.reflect.*;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.kloping.MySpringTool.partUtils.getExceptionLine;

/**
 * @author github-kloping
 */
public class HttpClientManagerImpl implements HttpClientManager {
    private abstract class M1<T> {
        private String path;

        private M1(String path) {
            this.path = path;
        }

        /**
         * proxy method run
         *
         * @param objects
         * @return
         */
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
        if (!cla.isInterface()) {
            return;
        }
        contextManager.append(cla, Proxy.newProxyInstance(cla.getClassLoader(),
                new Class[]{cla}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (methodInks.containsKey(method)) {
                            return methodInks.get(method).run(args);
                        }
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
        Method[] methods = null;
        if (method.isAnnotationPresent(Callback.class)) {
            Callback callback = method.getDeclaredAnnotation(Callback.class);
            String[] ss = callback.value();
            methods = parseMethods(ss);
        }
        if (type == 0) {
            Method[] finalMethods1 = methods;
            methodInks.put(method, new M1(url) {
                @Override
                Object run(Object... objects) {
                    try {
                        String trueUrl = getGetUrl(url, method, objects);
                        Connection connection = getConnection(trueUrl);
                        initCookie(connection, method, trueUrl, objects);
                        Class<?> cls = method.getReturnType();
                        if (cls == String.class)
                            return connection.get().toString();
                        if (cls == Document.class)
                            return connection.get();
                        if (cls == byte[].class)
                            return connection.method(Connection.Method.GET).execute().bodyAsBytes();
                        if (cls == CookieStore.class) {
                            connection.get();
                            return connection.cookieStore();
                        }
                        return Type(cls, connection.get(), finalMethods1);
                    } catch (Exception e) {
                        StarterApplication.logger.Log(getExceptionLine(e), -1);
                    }
                    return null;
                }
            });
        } else if (type == 1) {
            Method[] finalMethods = methods;
            methodInks.put(method, new M1(url) {
                @Override
                Object run(Object... objects) {
                    try {
                        String trueUrl = getGetUrl(url, method, objects);
                        String body = getPostBody(method, objects);
                        Connection connection = getConnection(trueUrl);
                        connection.requestBody(body);
                        connection.data(body);
                        initCookie(connection, method, trueUrl, objects);
                        Class<?> cls = method.getReturnType();
                        if (cls == String.class) {
                            return connection.post().toString();
                        }
                        if (cls == Document.class) {
                            return connection.post();
                        }
                        if (cls == byte[].class) {
                            return connection.method(Connection.Method.POST).execute().bodyAsBytes();
                        }
                        if (cls == CookieStore.class) {
                            connection.post();
                            return connection.cookieStore();
                        }
                        return Type(cls, connection.post(), finalMethods);
                    } catch (Exception e) {
                        StarterApplication.logger.Log(e.getMessage() + getExceptionLine(e), -1);
                    }
                    return null;
                }
            });
        }
    }

    private Method[] parseMethods(String[] ss) {
        Method[] methods = new Method[ss.length];
        int i = 0;
        for (String s : ss) {
            int i0 = s.lastIndexOf(".");
            String className = s.substring(0, i0);
            String methodName = s.substring(i0 + 1, s.length());
            try {
                Class<?> cla = Class.forName(className);
                Method method = null;
                for (Method declaredMethod : cla.getDeclaredMethods()) {
                    if (declaredMethod.getName().equals(methodName))
                        method = declaredMethod;
                }
                if (method == null) {
                    methods[i] = null;
                    continue;
                }
                method.setAccessible(true);
                methods[i] = method;
            } catch (Exception e) {
                StarterApplication.logger.Log(e.getMessage() + getExceptionLine(e), -1);
            }
            i++;
        }
        return methods;
    }

    private void initCookie(Connection connection, Method method, String trueUrl, Object[] objects) throws Exception {
        CookieStore cookieStore = connection.cookieStore();
        if (method.isAnnotationPresent(CookieFrom.class)) {
            CookieFrom cf = method.getAnnotation(CookieFrom.class);
            cookieStore = getCookieStore(cf.value(), Connection.Method.valueOf(cf.method()), trueUrl);
            connection.cookieStore(cookieStore);
        }
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class cla = parameter.getType();
            if (cla == CookieStore.class) {
                CookieStore store = (CookieStore) objects[i];
                addCookieStore(store, cookieStore);
            } else if (ObjectUtils.isSuperOrInterface(cla, Collection.class)) {
                ParameterizedType type = (ParameterizedType) parameter.getParameterizedType();
                Class t1 = (Class) type.getActualTypeArguments()[0];
                if (t1 == CookieStore.class) {
                    Collection<CookieStore> cookieStores = (Collection<CookieStore>) objects[i];
                    for (CookieStore store : cookieStores) {
                        addCookieStore(store, cookieStore);
                    }
                }
            }
        }
    }


    private static void addCookieStore(CookieStore from, CookieStore to) throws IllegalAccessException, NoSuchFieldException {
        Field field = from.getClass().getDeclaredField("uriIndex");
        field.setAccessible(true);
        Map<URI, List<HttpCookie>> uriIndex = (Map<URI, List<HttpCookie>>) field.get(from);
        List<HttpCookie> httpCookies = from.getCookies();
        uriIndex.forEach((k, v) -> {
            for (HttpCookie httpCookie : v) {
                to.add(k, httpCookie);
            }
        });
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
                    default:
                        break;
                }
            }
        }
        return sb.toString();
    }

    private static final AutomaticWiringParamsImpl awp = new AutomaticWiringParamsImpl();

    private <T> T Type(Class<T> cls, final Document doc, Method[] methods) {
        String finalText = doc.toString();
        String text = finalText;
        if (methods != null) {
            for (Method method : methods) {
                if (method == null) {
                    continue;
                }
                try {
                    Object[] os = awp.wiring(method, doc, text);
                    text = method.invoke(null, os).toString();
                } catch (Exception e) {
                    StarterApplication.logger.Log(e.getMessage() + getExceptionLine(e), -1);
                }
            }
        }
        try {
            return JSON.parseObject(text == null ? finalText : text).toJavaObject(cls);
        } catch (Exception e) {
            StarterApplication.logger.Log(e.getMessage() + "The data returned by the request could not be converted to the specified type( " + cls.getName() + ")\n" + getExceptionLine(e), -1);
            return null;
        }
    }


    private CookieStore getCookieStore(String[] urls, Connection.Method method, String url) throws IOException, URISyntaxException {
        CookieStore store = null;
        for (String u1 : urls) {
            try {
                Connection connection = null;
                CookieStore sc1 = null;
                if (u1.trim().toLowerCase().equals("this")) {
                    u1 = url.trim();
                }
                connection = getConnection(u1);
                if (method == Connection.Method.GET) connection.get();
                else if (method == Connection.Method.POST) connection.post();
                sc1 = connection.cookieStore();
                if (store == null)
                    store = sc1;
                else {
                    List<HttpCookie> httpCookies = sc1.getCookies();
                    for (int i1 = 0; i1 < httpCookies.size(); i1++) {
                        store.add(store.getURIs().get(i1), httpCookies.get(i1));
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
        StringBuilder sbend = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        sbend.append(url);
        for (int i = 0; i < parameters.length; i++) {
            if (objects[i] instanceof Params) {
                Params params = (Params) objects[i];
                params.getParams().forEach((k, v) -> {
                    sb.append(k).append("=").append(v).append("&");
                });
            } else if (parameters[i].isAnnotationPresent(ParamName.class)) {
                ParamName pn = parameters[i].getAnnotation(ParamName.class);
                String k = pn.value();
                Object v = objects[i];
                if (v == null) {
                    if (parameters[i].isAnnotationPresent(DefaultValue.class)) {
                        DefaultValue value = parameters[i].getDeclaredAnnotation(DefaultValue.class);
                        v = value.value();
                    }
                } else {
                    v = v.toString();
                }
                sb.append(k).append("=").append(v).append("&");
            } else if (parameters[i].isAnnotationPresent(ParamBody.class)) {
                ParamBody pn = parameters[i].getAnnotation(ParamBody.class);
                JSONObject jo = JSON.parseObject(JSON.toJSONString(objects[i]));
                jo.forEach((k, v) -> {
                    sb.append(k).append("=").append(v).append("&");
                });
            } else if (parameters[i].isAnnotationPresent(PathValue.class)) {
                PathValue pn = parameters[i].getAnnotation(PathValue.class);
                if (!sbend.toString().endsWith("/"))
                    sbend.append("/");
                sbend.append(objects[i].toString());
            }
        }
        if (sb.toString().endsWith("?")) sb.delete(sb.length() - 1, sb.length());
        if (sb.toString().endsWith("&")) sb.delete(sb.length() - 1, sb.length());
        sbend.append("?");
        sbend.append(sb.toString());
        return sbend.toString();
    }
}
