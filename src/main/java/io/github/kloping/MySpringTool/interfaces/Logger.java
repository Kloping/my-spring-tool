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

    /**
     * info log
     * @param mess
     */
    default void info(String mess) {
        Log(mess, 1);
    }

    /**
     * warring log
     * @param mess
     */
    default void waring(String mess) {
        Log(mess, 2);
    }

    /**
     * normal log
     * @param mess
     */
    default void log(String mess) {
        Log(mess, 0);
    }

    /**
     * error log
     * @param mess
     */
    default void error(String mess) {
        Log(mess, -1);
    }
}
