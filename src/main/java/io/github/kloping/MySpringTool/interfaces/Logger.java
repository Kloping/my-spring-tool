package io.github.kloping.MySpringTool.interfaces;

/**
 * 日志输出接口
 */
public interface Logger {
    void Log(String mess, Integer level);

    default void setFormat(String format) {
    }

    default void setPrefix(String Prefix) {
    }

    int setLogLevel(int level);
}
