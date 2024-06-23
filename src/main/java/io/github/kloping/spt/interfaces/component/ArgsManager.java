package io.github.kloping.spt.interfaces.component;

/**
 * 参数管理
 */
public interface ArgsManager {
    void setArgsType(Class<?>... classes);

    boolean isLegal(Object... objects);

    Class<?>[] getArgTypes();
}
