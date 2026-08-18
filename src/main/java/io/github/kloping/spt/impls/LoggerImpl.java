package io.github.kloping.spt.impls;

import io.github.kloping.spt.interfaces.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Compatibility adapter from the project's Logger API to SLF4J.
 */
public class LoggerImpl implements Logger {
    private final org.slf4j.Logger delegate = LoggerFactory.getLogger("io.github.kloping.spt");
    private volatile String prefix = "[g@kst]";
    private volatile SimpleDateFormat format = new SimpleDateFormat("MM/dd-HH:mm:ss:SSS");
    private File file;
    private BufferedWriter writer;

    @Override
    public synchronized void setOutFile(String path) {
        closeWriter();
        file = path == null ? null : new File(path);
    }

    @Override
    public void setFormat(SimpleDateFormat format) {
        if (format != null) this.format = format;
    }

    @Override
    public synchronized void Log(String mess, Integer level) {
        int actualLevel = level == null ? 0 : level;

        String message = prefix + (mess == null ? "" : mess);
        switch (actualLevel) {
            case -1:
                delegate.error(message);
                break;
            case 2:
                delegate.warn(message);
                break;
            case 0:
            case 1:
            default:
                delegate.info(message);
                break;
        }
        writeCompatibilityFile(message);
    }

    private void writeCompatibilityFile(String message) {
        if (file == null) return;
        try {
            if (writer == null) writer = new BufferedWriter(new FileWriter(file, true));
            writer.write("[" + format.format(new Date()) + "]=>" + message);
            writer.newLine();
            writer.flush();
        } catch (IOException e) {
            delegate.warn("Unable to write compatibility log file", e);
        }
    }

    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix == null ? "" : prefix;
    }

    public synchronized void close() {
        closeWriter();
    }

    private void closeWriter() {
        if (writer != null) {
            try {
                writer.close();
            } catch (IOException ignored) {
            } finally {
                writer = null;
            }
        }
    }
}
