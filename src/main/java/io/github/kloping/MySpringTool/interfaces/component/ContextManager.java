package io.github.kloping.MySpringTool.interfaces.component;



public interface ContextManager {
    int append(Object obj);

    int append(Object obj,  String id);

    int append(Class<?> cla,Object obj,  String id);

    <T> T getContextEntity(Class<T> cla);

    <T> T getContextEntity(Class<T> cla,  String id);
}
