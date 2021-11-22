package io.github.kloping.MySpringTool.interfaces.component;

public interface ArgsManager {
    void setArgsType(Class<?>... classes);

    boolean isLegal(Object... objects);

    Class<?>[] getArgTypes();
}
