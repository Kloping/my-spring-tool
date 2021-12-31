package io.github.kloping.MySpringTool.h1.impl;

import io.github.kloping.MySpringTool.StarterApplication;
import io.github.kloping.MySpringTool.interfaces.component.ConfigFileManager;
import io.github.kloping.MySpringTool.interfaces.component.ContextManager;
import io.github.kloping.file.FileUtils;
import io.github.kloping.object.ObjectUtils;
import io.github.kloping.serialize.HMLObject;

import java.io.File;

/**
 * @author github-kloping
 */
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
        String string = FileUtils.getStringFromFile(file);
        HMLObject object = HMLObject.parseObject(string);
        object.getEntry().forEach((k, v) -> {
            contextManager.append(v.getClass(), v, k);
        });
    }

    private void loadTxt(String file) {
        String[] ss = FileUtils.getStringsFromFile(file);
        for (String s : ss) {
            String[] s2 = s.split("=");
            if (s2.length < 2) {
                continue;
            }
            String m1 = s.replace(s2[0] + "=", "");
            Object o = ObjectUtils.maybeType(m1.trim());
            contextManager.append(o, s2[0]);
        }
        StarterApplication.logger.Log("load config file ok for " + file, 0);
    }

    @Override
    public void load(File file) {
        load(file.getAbsolutePath());
    }
}
