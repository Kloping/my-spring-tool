package io.github.kloping.MySpringTool.interfaces;

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
    }
}
