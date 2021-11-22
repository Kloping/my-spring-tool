package io.github.kloping.MySpringTool.interfaces.component;


import com.sun.istack.internal.Nullable;

public interface ContextManager {
    int append(Object obj);

    int append(Object obj, @Nullable String id);

    int append(Class<?> cla,Object obj, @Nullable String id);

    <T> T getContextEntity(Class<T> cla);

    <T> T getContextEntity(Class<T> cla, @Nullable String id);
}
