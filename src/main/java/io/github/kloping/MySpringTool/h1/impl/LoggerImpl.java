package io.github.kloping.MySpringTool.h1.impl;

import io.github.kloping.MySpringTool.interfaces.Logger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author github-kloping
 */
public class LoggerImpl implements Logger {
    private int logLevel = 0;
    private SimpleDateFormat df = new SimpleDateFormat("MM/dd-HH:mm:ss:SSS");
    private final String prefix = "[github.kloping.ST]";

    private File file;

    @Override
    public void setOutFile(String path) {
        file = new File(path);
    }

    @Override
    public void setFormat(String format) {
        df = new SimpleDateFormat(format);
    }

    @Override
    public void Log(String mess, Integer level) {
        if (level != -1 && level < logLevel) return;
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
        } else if (level == 1) {
            info = ("\033[32m" + info + "\033[m");
        } else if (level == 2) {
            info = "\033[33m" + info + "\033[m";
        }

        if (level == -1) {
            System.err.println(info);
        } else {
            System.out.println(info);
        }
        try {
            BufferedWriter writer = getWriter();
            if (writer == null) return;
            writer.write(info);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private BufferedWriter writer = null;

    private BufferedWriter getWriter() {
        if (file != null && writer == null) {
            try {
                writer = new BufferedWriter(new FileWriter(file, true));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return writer;
    }

    @Override
    public int setLogLevel(int level) {
        return logLevel = level;
    }
}
