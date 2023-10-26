package io.github.kloping.MySpringTool.h1.impl;

import io.github.kloping.MySpringTool.interfaces.Logger;
import org.fusesource.jansi.Ansi;

import java.awt.*;
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
    public static final Color NORMAL_LOW_COLOR = new Color(116, 117, 116, 224);
    public static final Color NORMAL_COLOR = new Color(202, 206, 199, 247);
    public static final Color INFO_COLOR = new Color(24, 220, 85, 247);
    public static final Color DEBUG_COLOR = new Color(234, 213, 103, 247);
    public static final Color ERROR_COLOR = new Color(224, 17, 106, 247);

    private int logLevel = 0;
    private SimpleDateFormat df = new SimpleDateFormat("MM/dd-HH:mm:ss:SSS");
    private String prefix = "[github.kloping.ST]";

    private File file;

    @Override
    public void setOutFile(String path) {
        file = new File(path);
    }

    @Override
    public void setFormat(SimpleDateFormat format) {
        df = format;
    }

    @Override
    public void Log(String mess, Integer level) {
        String log = null;
        String out = null;
        try {
            log = "[" + df.format(new Date()) + "]" + "=>" + mess;
            switch (level) {
                case 0:
                    log = "[Normal]" + log;
                    break;
                case 1:
                    log = "[Info]  " + log;
                    break;
                case 2:
                    log = "[Debug] " + log;
                    break;
                case -1:
                    log = "[Error] " + log;
                    break;
                default:
            }
            log = prefix + log;
            out = null;
            if (level == 0) {
                out = Ansi.ansi().fgRgb(NORMAL_COLOR.getRGB()).a(log).reset().toString();
            } else if (level == 1) {
                out = Ansi.ansi().fgRgb(INFO_COLOR.getRGB()).a(log).reset().toString();
            } else if (level == 2) {
                out = Ansi.ansi().fgRgb(DEBUG_COLOR.getRGB()).a(log).reset().toString();
            } else if (level == -1) {
                out = Ansi.ansi().fgRgb(ERROR_COLOR.getRGB()).a(log).reset().toString();
            }
        } catch (Exception e) {
            if (level != -1 && level < logLevel) {
            } else e.printStackTrace();
        }
        try {
            BufferedWriter writer = getWriter();
            if (writer != null) {
                writer.write(log);
                writer.newLine();
                writer.flush();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (level != -1 && level < logLevel) return;
        System.out.println(out);
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

    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}
