package io.github.kloping.spt.impls;

import io.github.kloping.spt.Setting;
import io.github.kloping.spt.interfaces.Extension;
import io.github.kloping.spt.interfaces.Logger;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * @author github.kloping
 */
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
        Logger logger = setting.getContextManager().getContextEntity(Logger.class);
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
                if (logger != null)
                    logger.info(runnable.getName() + " extension load");
            } catch (ClassNotFoundException e) {
                cname.add(extension);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                if (logger != null)
                    logger.info(extension + " extension load failed");
            }
        }
        if (!cname.isEmpty()) {
            final String msg = "can't load extension(s) " + cname;
            if (logger != null) logger.error(msg);
            else System.err.println(msg);
        }
    }

    @Override
    public List<String> getExtensions() {
        return EXTENSIONS;
    }
}
