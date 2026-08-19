package io.github.kloping.spt.impls;

import lombok.extern.slf4j.Slf4j;
import io.github.kloping.spt.interfaces.component.ConfigFileManager;
import io.github.kloping.spt.interfaces.component.ContextManager;
import io.github.kloping.spt.util.HmlObject;
import io.github.kloping.spt.util.IoUtils;
import io.github.kloping.spt.util.ObjectUtils;

import java.io.File;

/**
 * @author github-kloping
 */
@Slf4j
public class ConfigFileManagerImpl implements ConfigFileManager {
    private ContextManager contextManager;


    public ConfigFileManagerImpl(ContextManager contextManager) {
        this.contextManager = contextManager;
    }

    private static final String SUFFIX = ".hml";

    @Override
    public void load(String file) {
        if (!file.endsWith(SUFFIX)) {
            loadTxt(file);
        } else {
            loadHml(file);
        }
    }

    private void loadHml(String file) {
        String string;
        try {
            string = IoUtils.readString(file);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Unable to read config file: " + file, e);
        }
        HmlObject object = HmlObject.parse(string);
        object.getEntry().forEach((k, v) -> {
            Object value = v instanceof HmlObject ? ((HmlObject) v).toJavaObject() : v;
            if (value != null) contextManager.append(value.getClass(), value, k);
        });
    }

    private void loadTxt(String file) {
        String[] ss;
        try {
            ss = IoUtils.readLines(file);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Unable to read config file: " + file, e);
        }
        for (String s : ss) {
            String[] s2 = s.split("=");
            if (s2.length < 2) {
                continue;
            }
            String m1 = s.replace(s2[0] + "=", "");
            Object o = ObjectUtils.maybeType(m1.trim());
            contextManager.append(o, s2[0]);
        }
        log.debug("load config file ok for {}", file);
    }

    @Override
    public void load(File file) {
        load(file.getAbsolutePath());
    }
}
