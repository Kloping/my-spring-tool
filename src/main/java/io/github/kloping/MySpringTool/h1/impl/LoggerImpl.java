package io.github.kloping.MySpringTool.h1.impl;

import io.github.kloping.MySpringTool.interfaces.Logger;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author github-kloping
 */
public class LoggerImpl implements Logger {
    private int Log_Level = 0;
    private SimpleDateFormat df = new SimpleDateFormat("MM/dd-HH:mm:ss:SSS");
    private final String prefix = "[github.kloping.ST]";

    @Override
    public void setFormat(String format) {
        df = new SimpleDateFormat(format);
    }

    @Override
    public void Log(String mess, Integer level) {
        if (level != -1 && level < Log_Level) return;
        String info = "[" + df.format(new Date()) + "]" + "=>" + mess;
        switch (level) {
            case 0:
                info = "[Normal]" + info;
                break;
            case 1:
                info = "[Info]  " + info;
                break;
            case 2:
                info = "[Debug] " + info;
                break;
            case -1:
                info = "[Error] " + info;
                break;
            default:
        }
        info = prefix + info;
        if (level == 0) {
            System.out.println(info);
        } else if (level == 1) {
            System.out.println("\033[32m" + info + "\033[m");
        } else if (level == 2) {
            System.out.println("\033[33m" + info + "\033[m");
        } else if (level == -1) {
            System.err.println(info);
        }
    }

    @Override
    public int setLogLevel(int level) {
        return Log_Level = level;
    }
}
