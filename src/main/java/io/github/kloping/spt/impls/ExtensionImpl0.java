package io.github.kloping.spt.impls;

import io.github.kloping.spt.Setting;
import io.github.kloping.spt.interfaces.Extension;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author github.kloping
 */
@Slf4j
public class ExtensionImpl0 implements Extension {
    public static final List<String> EXTENSIONS = new ArrayList<>();

    static {
        EXTENSIONS.add("io.github.kloping.little_web.WebExtension");
        EXTENSIONS.add("io.github.kloping.spt.SptRedis");
    }

    private Setting setting;

    public static ExtensionImpl0 INSTANCE = null;

    public ExtensionImpl0(Setting setting) {
        this.setting = setting;
        load();
    }

    private void load() {
        List<String> cname = new LinkedList<>();
        for (String extension : getExtensions()) {
            ExtensionRunnable runnable = null;
            try {
                Class<ExtensionRunnable> cla = (Class<ExtensionRunnable>) Class.forName(extension);
                Object o = setting.getInstanceCrater().create(cla, setting.getContextManager());
                setting.getContextManager().append(o, extension);
                runnable = (ExtensionRunnable) o;
                runnable.setSetting(setting);
                runnable.run();
                log.debug("{} extension load", runnable.getName());
            } catch (ClassNotFoundException e) {
                cname.add(extension);
            } catch (Throwable throwable) {
                log.error("{} extension load failed", extension, throwable);
            }
        }
        if (!cname.isEmpty()) {
            final String msg = "can't load extension(s) " + cname;
            log.debug(msg);
        }
    }

    @Override
    public List<String> getExtensions() {
        return EXTENSIONS;
    }
}
