package io.github.kloping.MySpringTool.h1.impl;

import io.github.kloping.MySpringTool.StarterApplication;
import io.github.kloping.MySpringTool.interfaces.Extension;

import java.util.ArrayList;
import java.util.List;

/**
 * @author github.kloping
 */
public class ExtensionImpl0 implements Extension {
    public static final List<String> EXTENSIONS = new ArrayList<>();

    static {
        EXTENSIONS.add("io.github.kloping.little_web.WebExtension");
    }

    public static ExtensionImpl0 INSTANCE = null;

    public ExtensionImpl0() {
        load();
    }

    private void load() {
        for (String extension : getExtensions()) {
            ExtensionRunnable runnable = null;
            try {
                Class<ExtensionRunnable> cla = (Class<ExtensionRunnable>) Class.forName(extension);
                Object o = StarterApplication.Setting.INSTANCE.getInstanceCrater().create(cla, StarterApplication.Setting.INSTANCE.getContextManager());
                StarterApplication.Setting.INSTANCE.getContextManager().append(o, extension);
                runnable = (ExtensionRunnable) o;
                runnable.run();
                StarterApplication.logger.info(runnable.getName() + " extension load");
            } catch (ClassNotFoundException e) {

            } catch (InstantiationException e) {

            } catch (IllegalAccessException e) {

            } catch (Throwable throwable) {
                throwable.printStackTrace();
                StarterApplication.logger.info(extension + " extension load failed");
            }
        }
    }

    @Override
    public List<String> getExtensions() {
        return EXTENSIONS;
    }
}
