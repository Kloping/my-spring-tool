package io.github.kloping.spt.impls;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.github.kloping.io.ReadUtils;
import io.github.kloping.reg.MatcherUtils;
import io.github.kloping.spt.Setting;
import io.github.kloping.spt.annotations.http.*;
import io.github.kloping.spt.annotations.http.Callback;
import io.github.kloping.spt.annotations.http.Headers;
import io.github.kloping.spt.annotations.http.RequestBody;
import io.github.kloping.spt.entity.KeyVals;
import io.github.kloping.spt.entity.Params;
import io.github.kloping.spt.interfaces.Logger;
import io.github.kloping.spt.interfaces.component.ClassManager;
import io.github.kloping.spt.interfaces.component.ContextManager;
import io.github.kloping.spt.interfaces.component.HttpClientManager;
import okhttp3.*;
import org.fusesource.jansi.Ansi;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.helper.HttpConnection;
import org.jsoup.nodes.Document;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.*;
import java.net.CookieStore;
import java.net.HttpCookie;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static io.github.kloping.spt.PartUtils.getExceptionLine;

/**
 * @author github-kloping
 */
public class HttpClientManagerImpl implements HttpClientManager {
    public static final String SPLIT = "/";
    private static final org.slf4j.Logger log = LoggerFactory.getLogger(HttpClientManagerImpl.class);
    private Setting setting;
    private Map<Method, Invoker> methodInks = new ConcurrentHashMap<>();
    private static final OkHttpClient OK_HTTP_CLIENT = new OkHttpClient.Builder().connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS).writeTimeout(10, TimeUnit.SECONDS).build();

    private class Invoker {
        private String path;
        private String host;
        /**
         * @Req 注解方法
         */
        private Method method;

        private Connection.Method type;
        /**
         * 处理过滤的方法
         */
        private Method[] methods;

        public Invoker(String host, String path, Method method, Connection.Method type, Method[] methods) {
            this.path = path;
            this.host = host;
            this.method = method;
            this.type = type;
            this.methods = methods;
        }

        private Request.Builder requestBuilder;
        private String finalUrl;

        /**
         * proxy method run
         *
         * @param objects
         * @return
         */
        private Object run(Object... objects) throws Throwable {
            long start = System.currentTimeMillis();
            long cost = -1L;
            Class<?> rtype = method.getReturnType();
            Class dType = method.getDeclaringClass();
            try {
                if (requestBuilder == null) {
                    requestBuilder = new Request.Builder();
                    finalUrl = getGetUrl(host, path, method, objects);
                    requestBuilder.url(finalUrl);
                }
                getHeaders(method, objects).forEach((k, v) -> requestBuilder.header(k, v));
                requestBuilder.header("User-Agent", userAgent);
                okhttp3.RequestBody requestBody = loadBody(method, objects);
                MultipartBody multipartBody = loadData(method, objects);
                if (type == Connection.Method.POST) {
                    if (requestBody != null) requestBuilder.post(requestBody);
                    if (multipartBody != null) requestBuilder.post(multipartBody);
                }
                Call call = OK_HTTP_CLIENT.newCall(requestBuilder.build());
                cost = System.currentTimeMillis();
                Response response = call.execute();
                cost = System.currentTimeMillis() - cost;
                int status = response.code();

                String statusTips = null;
                if (status < 200 || status >= 400) {
                    statusTips = Ansi.ansi().fgRgb(LoggerImpl.ERROR_COLOR.getRGB()).a(status).reset().toString();
                    logger.error(new HttpStatusException("HTTP error fetching URL",
                            status, response.request().url().url().toString()).getMessage());
                } else statusTips = Ansi.ansi().fgRgb(LoggerImpl.INFO_COLOR.getRGB()).a(status).reset().toString();
                if (print)
                    logger.log(String.format("resp status code %s from the [%s]", statusTips, response.request().url().url()));

                byte[] outBytes = response.body().bytes();
                Document doc = Jsoup.parse(new String(outBytes));
                Object o = null;
                if (rtype == void.class) o = null;
                else if (rtype == Document.class) o = doc;
                else if (rtype == byte[].class) o = outBytes;
                else o = toType(rtype, doc, response, methods);
                Object finalO = o;
                for (HttpStatusReceiver receiver : receivers)
                    receiver.receive(HttpClientManagerImpl.this, finalUrl, status, dType, method, type, rtype, finalO, doc);
                return o;
            } catch (Throwable e) {
                for (HttpStatusReceiver receiver : receivers)
                    receiver.receive(HttpClientManagerImpl.this, merge(host, path), 0, dType, method, type, rtype, null, null);
                throw e;
            } finally {
                logger.log(String.format("The entire proxy request process took %sms.(execute okhttp request cost %sms)"
                        , System.currentTimeMillis() - start, cost));
            }
        }
    }

    private boolean print = true;

    @Override
    public void setPrint(boolean k) {
        print = k;
    }

    /**
     * 将数据转为最终类型
     *
     * @param cls     最终类型
     * @param methods 代理方法 Callback
     * @param <T>
     * @return
     */
    private <T> T toType(Class<T> cls, Document doc, Response resp, Method[] methods) throws IOException {
        String finalText = doc.body().text();
        String text = finalText;
        if (methods != null) {
            for (Method method : methods) {
                if (method == null) continue;
                try {
                    Object[] os = AWP.wiring(method, text);
                    Object out = method.invoke(null, os);
                    if (out.getClass() == String.class) {
                        text = out.toString();
                    } else if (out.getClass() == cls) {
                        return (T) out;
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage() + getExceptionLine(e));
                }
            }
        }
        try {
            String data = (text == null ? finalText : text);
            if (print)
                logger.log(String.format("Get the data [%s] from the [%s]", Ansi.ansi().fgRgb(LoggerImpl.NORMAL_LOW_COLOR.getRGB()).a(data).reset().toString(),
                        resp.request().url().url()));
            if (cls == String.class) {
                return (T) data;
            } else if (cls.isArray()) {
                return JSON.parseArray(data).toJavaObject(cls);
            } else {
                return JSON.parseObject(data).toJavaObject(cls);
            }
        } catch (Exception e) {
            logger.error(e.getMessage() == null ? "" :
                    e.getMessage() + "The data returned by the request could not be converted to " +
                            "the specified type( " + cls.getName() + ")\n" + getExceptionLine(e));
            return null;
        }
    }

    private Logger logger;

    public HttpClientManagerImpl(Setting setting, ClassManager classManager) {
        this.setting = setting;
        classManager.registeredAnnotation(HttpClient.class, this);
        setting.getSTARTED_RUNNABLE().add(() -> {
            logger = setting.getContextManager().getContextEntity(Logger.class);
        });
    }

    private List<HttpStatusReceiver> receivers = new LinkedList<>();

    @Override
    public void addHttpStatusReceiver(HttpStatusReceiver receiver) {
        receivers.add(receiver);
    }

    @Override
    public void manager(Method method, ContextManager contextManager) throws IllegalAccessException, InvocationTargetException {
        method.setAccessible(true);
        String host = method.getDeclaringClass().getDeclaredAnnotation(HttpClient.class).value();
        Connection.Method mt = null;
        String path = null;
        if (method.isAnnotationPresent(GetPath.class)) {
            path = method.getAnnotation(GetPath.class).value();
            mt = Connection.Method.GET;
        } else if (method.isAnnotationPresent(PostPath.class)) {
            path = method.getAnnotation(PostPath.class).value();
            mt = Connection.Method.POST;
        } else if (method.isAnnotationPresent(RequestPath.class)) {
            RequestPath rp = method.getAnnotation(RequestPath.class);
            path = rp.value();
            mt = rp.method();
        }
        loadMethod(host, path, method, mt);
    }

    private String merge(String host, String path) throws Throwable {
        if (host.matches(".*?\\{.*?}.*?")) {
            for (String r0 : MatcherUtils.matcherAll(host, "\\{.*?}")) {
                String fielda = r0.substring(1, r0.length() - 1);
                AccessibleObject aco = parse(fielda);
                Object value = getValue(aco);
                host = host.replace(r0, value.toString());
            }
        }
        if (!host.endsWith(SPLIT)) {
            host += SPLIT;
        }
        if (path.startsWith(SPLIT)) {
            path = path.substring(1, path.length());
        }
        if (path.endsWith(SPLIT)) {
            path = path.substring(0, path.length() - 1);
        }
        return host + path;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        Object o = null;
        if (methodInks.containsKey(method)) o = methodInks.get(method).run(args);
        return o;
    }

    @Override
    public void manager(Class cla, ContextManager contextManager) throws IllegalAccessException, InvocationTargetException {
        if (!cla.isInterface()) return;
        contextManager.append(cla, Proxy.newProxyInstance(cla.getClassLoader(), new Class[]{cla}, this), UUID.randomUUID().toString());
        for (Method declaredMethod : cla.getDeclaredMethods()) {
            this.manager(declaredMethod, contextManager);
        }
    }

    private void loadMethod(String host, String path, Method method, Connection.Method type) {
        Method[] methods = null;
        if (method.isAnnotationPresent(Callback.class)) {
            Callback callback = method.getDeclaredAnnotation(Callback.class);
            String[] ss = callback.value();
            methods = loadMethods(ss);
        }
        Method[] finalMethods = methods;
        methodInks.put(method, new Invoker(host, path, method, type, methods));
    }

    private okhttp3.RequestBody loadBody(Method method, Object[] objects) {
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            Parameter parameter = parameters[i];
            Class cla = parameter.getType();
            if (parameter.isAnnotationPresent(RequestBody.class)) {
                RequestBody rb = parameter.getAnnotation(RequestBody.class);
                switch (rb.type()) {
                    case toString:
                        return okhttp3.RequestBody.create(objects[i].toString(), MediaType.parse("text/plain"));
                    case json:
                        if (objects[i].getClass().isAssignableFrom(String.class)) {
                            return okhttp3.RequestBody.create(objects[i].toString(), MediaType.parse("application/json"));
                        } else {
                            return okhttp3.RequestBody.create(JSON.toJSONString(objects[i]), MediaType.parse("application/json"));
                        }
                    default:
                        break;
                }
            }
        }
        return null;
    }

    private MultipartBody loadData(Method method, Object[] objects) throws IOException {
        MultipartBody.Builder multipartBody = null;
        Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            if (objects[i] == null) continue;
            Parameter parameter = parameters[i];
            Class cla = parameter.getType();
            if (parameter.isAnnotationPresent(RequestData.class)) {
                if (cla == Entry.class) {
                    Entry entry = (Entry) objects[i];
                    if (multipartBody == null) multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
                    multipartBody.addFormDataPart(entry.getKey().toString(), entry.getValue().toString());
                } else if (cla == String.class) {
                    if (multipartBody == null) multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
                    multipartBody.addPart(okhttp3.RequestBody.create(objects[i].toString(), MediaType.parse("text/plain")));
                } else if (objects[i] instanceof KeyVals) {
                    KeyVals data = (KeyVals) objects[i];
                    for (HttpConnection.KeyVal value : data.values()) {
                        if (multipartBody == null)
                            multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
                        if (value.hasInputStream()) {
                            byte[] bytes = ReadUtils.readAll(value.inputStream());
                            multipartBody.addFormDataPart(value.key(), value.value(),
                                    okhttp3.RequestBody.create(bytes, MediaType.parse(value.contentType())));
                        } else {
                            multipartBody.addFormDataPart(value.key(), value.value());
                        }
                    }
                }
            } else if (parameter.isAnnotationPresent(FileParm.class)) {
                if (cla == byte[].class) {
                    byte[] bytes = (byte[]) objects[i];
                    FileParm fileParm = parameter.getDeclaredAnnotation(FileParm.class);
                    if (multipartBody == null)
                        multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM);

                    if (!fileParm.type().isEmpty()) {
                        multipartBody.addFormDataPart(fileParm.value(), fileParm.name(),
                                okhttp3.RequestBody.create(bytes, MediaType.parse(fileParm.type())));
                    } else {
                        multipartBody.addFormDataPart(fileParm.value(), fileParm.name(),
                                okhttp3.RequestBody.create(bytes, MediaType.parse(fileParm.type())));
                    }
                } else if (cla == HttpConnection.KeyVal.class) {
                    HttpConnection.KeyVal value = (HttpConnection.KeyVal) objects[i];
                    if (multipartBody == null)
                        multipartBody = new MultipartBody.Builder().setType(MultipartBody.FORM);
                    if (value.hasInputStream()) {
                        byte[] bytes = ReadUtils.readAll(value.inputStream());
                        multipartBody.addFormDataPart(value.key(), value.value(),
                                okhttp3.RequestBody.create(bytes, MediaType.parse(value.contentType())));
                    } else {
                        multipartBody.addFormDataPart(value.key(), value.value());
                    }
                }
            }
        }
        if (multipartBody != null) return multipartBody.build();
        else return null;
    }

    private Method[] loadMethods(String[] ss) {
        int i = 0;
        Method[] methods = new Method[ss.length];
        for (String s : ss) {
            int i0 = s.lastIndexOf(".");
            String methodName = s.substring(i0 + 1, s.length());
            String className = s.substring(0, i0);
            try {
                Class<?> cla = this.getClass().getClassLoader().loadClass(className);
                Method method = null;
                for (Method declaredMethod : cla.getDeclaredMethods()) {
                    if (declaredMethod.getName().equals(methodName)) {
                        method = declaredMethod;
                    }
                }
                if (method == null) {
                    methods[i] = null;
                    continue;
                }
                method.setAccessible(true);
                methods[i] = method;
            } catch (Exception e) {
                logger.error(e.getMessage() + getExceptionLine(e));
            }
            i++;
        }
        return methods;
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

    public static String userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36 Edg/95.0.1020.53";

    private String loadPostBody(Method method, Object[] objects) throws Exception {
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

    private static final AutomaticWiringParamsImpl AWP = new AutomaticWiringParamsImpl();

    private CookieStore getCookieStore(String[] urls, Connection.Method method, String url, Method m0, Object... objects) throws IOException, URISyntaxException {
//        CookieStore store = null;
//        for (String u1 : urls) {
//            try {
//                Connection connection = null;
//                CookieStore sc1 = null;
//                if ("this".equals(u1.trim().toLowerCase())) {
//                    u1 = url.trim();
//                }
//                connection = getConnection(m0, u1, getHeaders(m0, objects));
//                if (method == Connection.Method.GET) {
//                    connection.get();
//                } else if (method == Connection.Method.POST) {
//                    connection.post();
//                }
//                sc1 = connection.cookieStore();
//                if (store == null) {
//                    store = sc1;
//                } else {
//                    List<HttpCookie> httpCookies = sc1.getCookies();
//                    for (int i1 = 0; i1 < httpCookies.size(); i1++) {
//                        store.add(store.getURIs().get(i1), httpCookies.get(i1));
//                    }
//                }
//            } catch (Exception e) {
//                logger.error("get Cookie Failed From: " + u1);
//                continue;
//            }
//        }
//        return store;
        return null;
    }

    private String getGetUrl(String host, String path, Method method, Object... objects) throws Throwable {
        Parameter[] parameters = method.getParameters();
        StringBuilder urlsb = new StringBuilder();
        StringBuilder sb = new StringBuilder();
        Map<String, Object> replaceMap = new HashMap<>();
        urlsb.append(merge(host, path));
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
                if (pn.value() == null || pn.value().isEmpty()) {
                    if (!urlsb.toString().endsWith(SPLIT)) {
                        urlsb.append(SPLIT);
                    }
                    urlsb.append(objects[i].toString());
                } else {
                    String name = pn.value();
                    name = "{" + name + "}";
                    replaceMap.put(name, objects[i]);
                }
            }
        }
        if (sb.toString().endsWith(DO)) {
            sb.delete(sb.length() - 1, sb.length());
        }
        if (sb.toString().endsWith(AND)) {
            sb.delete(sb.length() - 1, sb.length());
        }
        urlsb.append(DO);
        urlsb.append(sb.toString());
        String url0 = urlsb.toString();
        if (url0.startsWith(SPLIT)) {
            url0 = url0.substring(1);
        }
        if (url0.endsWith(DO)) {
            url0 = url0.substring(0, url0.length() - 1);
        }
        for (String s : replaceMap.keySet()) {
            url0 = url0.replace(s, replaceMap.get(s).toString());
        }
        if (url0.startsWith(SPLIT)) {
            url0 = url0.substring(1);
        }
        return url0;
    }

    private Map<String, String> getHeaders(Method method, Object... objects) {
        Class cn0 = method.getDeclaringClass();
        Parameter[] parameters = method.getParameters();
        Map<String, String> map = new HashMap<>();
        int i = 0;
        for (Parameter parameter : parameters) {
            if (parameter.isAnnotationPresent(Headers.class)) {
                try {
                    if (Map.class.isAssignableFrom(parameter.getType())) {
                        Map<String, String> map2 = (Map) objects[i];
                        if (map2.keySet().iterator().next() instanceof String) {
                            if (map2.values().iterator().next() instanceof String) {
                                map = map2;
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error("The Parameter Type not is Map<String,String>");
                }
            }
            i++;
        }
        if (cn0.isAnnotationPresent(Headers.class)) {
            Headers headers = method.getDeclaringClass().getDeclaredAnnotation(Headers.class);
            String s = headers.value();
            if (!(s == null || s.isEmpty())) {
                try {
                    AccessibleObject field = parse(s);
                    Object o = getValue(field);
                    if (o instanceof Map) {
                        Map<String, String> m = (Map<String, String>) o;
                        map.putAll(m);
                    }
                } catch (Throwable e) {
                    e.printStackTrace();
                    logger.error("parse error at " + cn0 + " Annotation @Headers");
                }
            }
        }
        return map;
    }

    /**
     * @param ao
     * @return
     * @throws Throwable
     */
    private Object getValue(AccessibleObject ao) throws Throwable {
        if (ao == null) return null;
        ao.setAccessible(true);
        ContextManager contextManager = setting.getContextManager();
        if (ao instanceof Method) {
            Method method = (Method) ao;
            Object o = method.invoke(contextManager.getContextEntity(method.getDeclaringClass()));
            return o;
        } else if (ao instanceof Field) {
            Field field = (Field) ao;
            Object o = field.get(contextManager.getContextEntity(field.getDeclaringClass()));
            return o;
        }
        return null;
    }

    /**
     * 将类似 test.Main.s0 字段解析为 可获取的实例
     *
     * @param str
     * @return
     * @throws ClassNotFoundException
     */
    private static final AccessibleObject parse(String str) throws ClassNotFoundException {
        AccessibleObject accessibleObject = null;
        int i0 = str.lastIndexOf(".");
        String clan = str.substring(0, i0);
        String fn = str.substring(i0 + 1);
        Class cl0 = Class.forName(clan);
        try {
            accessibleObject = cl0.getDeclaredField(fn);
        } catch (NoSuchFieldException e) {
        }
        if (accessibleObject == null) {
            try {
                accessibleObject = cl0.getDeclaredMethod(fn);
            } catch (NoSuchMethodException e) {
            }
        }
        return accessibleObject;
    }

    private static final String AND = "&";
    private static final String DO = "?";
}
