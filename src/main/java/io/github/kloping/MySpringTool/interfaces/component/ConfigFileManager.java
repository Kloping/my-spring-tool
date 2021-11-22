package io.github.kloping.MySpringTool.interfaces.component;

import java.io.File;

/**
 * 配置文件管理器
 */
public interface ConfigFileManager {
    void load(String file);

    void load(File file);
}
