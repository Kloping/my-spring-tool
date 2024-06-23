package io.github.kloping.spt.interfaces.component;

import java.io.File;

/**
 * 配置文件管理器
 */
public interface ConfigFileManager {
    void load(String file);

    void load(File file);
}
