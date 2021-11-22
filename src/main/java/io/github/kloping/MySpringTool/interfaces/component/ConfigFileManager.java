package io.github.kloping.MySpringTool.interfaces.component;

import java.io.File;

public interface ConfigFileManager {
    void load(String file);

    void load(File file);
}
