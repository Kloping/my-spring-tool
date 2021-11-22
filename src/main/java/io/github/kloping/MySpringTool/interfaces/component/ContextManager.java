package io.github.kloping.MySpringTool.interfaces.component;


/**
 * 全局上下文 Bean 管理器
 */
public interface ContextManager {
    int append(Object obj);

    int append(Object obj,  String id);

    int append(Class<?> cla,Object obj,  String id);

    <T> T getContextEntity(Class<T> cla);

    <T> T getContextEntity(Class<T> cla,  String id);
}
