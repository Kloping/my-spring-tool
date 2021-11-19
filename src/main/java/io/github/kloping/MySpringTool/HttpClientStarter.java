package io.github.kloping.MySpringTool;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.github.kloping.MySpringTool.annotations.http.*;
import io.github.kloping.MySpringTool.entity.Params;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.lang.reflect.Proxy;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static io.github.kloping.MySpringTool.Starter.Log;
import static io.github.kloping.MySpringTool.Starter.appendToObjMap;
import static io.github.kloping.MySpringTool.partUtils.getExceptionLine;

final class HttpClientStarter {
    private static abstract class M1<T> {
        private String path;

        private M1(String path) {
            this.path = path;
        }

        abstract T run(Object... objects);
    }

    static <T> void InitHttpClientInterface(Class<T> cla) {
        appendToObjMap(UUID.randomUUID().toString(), Proxy.newProxyInstance(cla.getClassLoader(),
                new Class[]{cla}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (methodInks.containsKey(method))
                            return methodInks.get(method).run(args);
                        return null;
                    }
                }), cla);
        String host = cla.getAnnotation(HttpClient.class).value();
        Method[] methods = cla.getDeclaredMethods();
        for (Method method : methods) {
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
    }

    private static String ali(String host, String path) {
        if (!host.endsWith("/")) host += "/";
        if (path.startsWith("/")) path = path.substring(1, path.length());
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        return host + path;
    }

    private static Map<Method, M1> methodInks = new ConcurrentHashMap<>();

    private static void InitMethod(Method method, String url, int type) {
        if (type == 0) {
            methodInks.put(method, new M1(url) {
                @Override
                Object run(Object... objects) {
                    try {
                        String trueUrl = getGetUrl(url, method, objects);
                        Document document = Jsoup.connect(trueUrl).ignoreContentType(true)
                                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36 Edg/95.0.1020.53")
                                .get();
                        Class<?> cls = method.getReturnType();
                        if (cls == String.class)
                            return document.toString();
                        else return Type(cls, document.body().text());
                    } catch (Exception e) {
                        Log(getExceptionLine(e), -1);
                    }
                    return null;
                }
            });
        } else if (type == 1) {
            methodInks.put(method, new M1(url) {
                @Override
                Object run(Object... objects) {
                    try {
                        String body = getPostBody(method, objects);
                        Document document = Jsoup.connect(url).ignoreContentType(true)
                                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36 Edg/95.0.1020.53")
                                .requestBody(body)
                                .post();
                        Class<?> cls = method.getReturnType();
                        if (cls == String.class)
                            return document.toString();
                        else return Type(cls, document.body().text());
                    } catch (Exception e) {
                        Log(getExceptionLine(e), -1);
                    }
                    return null;
                }
            });
        }
    }

    private static String getPostBody(Method method, Object[] objects) {
        Parameter[] parameters = method.getParameters();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(RequestBody.class)) {
                RequestBody rb = parameters[i].getAnnotation(RequestBody.class);
                String type = rb.type();
                if (type.equals("toString")) {
                    sb.append(objects[i]);
                } else if (type.equals("json")) {
                    sb.append(JSON.toJSONString(objects[i]));
                }
            } else if (objects[i] instanceof Params) {
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
        if (sb.toString().endsWith("&")) sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }


    private static <T> T Type(Class<T> cls, String text) {
        return JSON.parseObject(text).toJavaObject(cls);
    }

    private static String getGetUrl(String url, Method method, Object... objects) {
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
        if (sb.toString().endsWith("&")) sb.delete(sb.length() - 1, sb.length());
        return sb.toString();
    }
}
