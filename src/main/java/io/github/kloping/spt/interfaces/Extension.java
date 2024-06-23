package io.github.kloping.spt.interfaces;

import io.github.kloping.spt.Setting;

import java.util.List;

/**
 * @author github.kloping
 */
public interface Extension {
    /**
     * get extension class names
     *
     * @return
     */
    List<String> getExtensions();

    public static interface ExtensionRunnable {
        /**
         * extension run
         *
         * @throws Throwable
         */
        void run() throws Throwable;

        /**
         * get extension name
         *
         * @return
         */
        String getName();

        /**
         * on setting
         *
         * @param setting
         */
        void setSetting(Setting setting);
    }
}
