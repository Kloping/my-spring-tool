package io.github.kloping.MySpringTool.interfaces;

import java.text.SimpleDateFormat;

/**
 * 日志输出接口
 */
public interface Logger {
    void Log(String mess, Integer level);

    default void setFormat(SimpleDateFormat format) {
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
     *
     * @param mess
     */
    default void error(String mess) {
        Log(mess, -1);
    }

    /**
     * out file
     *
     * @param path
     */
    void setOutFile(String path);
}
