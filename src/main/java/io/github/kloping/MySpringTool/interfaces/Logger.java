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

    default void info(String mess) {
        Log(mess, 1);
    }

    default void waring(String mess) {
        Log(mess, 2);
    }

    default void log(String mess) {
        Log(mess, 0);
    }

    default void eror(String mess) {
        Log(mess, -1);
    }
}
