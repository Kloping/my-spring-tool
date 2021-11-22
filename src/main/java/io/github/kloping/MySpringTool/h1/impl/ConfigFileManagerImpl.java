package io.github.kloping.MySpringTool.h1.impl;

import io.github.kloping.MySpringTool.StarterApplication;
import io.github.kloping.MySpringTool.interfaces.component.ConfigFileManager;
import io.github.kloping.MySpringTool.interfaces.component.ContextManager;
import io.github.kloping.file.FileUtils;
import io.github.kloping.object.ObjectUtils;

import java.io.File;

public class ConfigFileManagerImpl implements ConfigFileManager {
    private ContextManager contextManager;


    public ConfigFileManagerImpl(ContextManager contextManager) {
        this.contextManager = contextManager;
    }

    @Override
    public void load(String file) {
        String[] ss = FileUtils.getStringsFromFile(file);
        for (String s : ss) {
            String[] s2 = s.split("=");
            if (s2.length < 2) continue;
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
