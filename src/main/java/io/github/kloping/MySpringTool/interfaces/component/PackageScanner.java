package io.github.kloping.MySpringTool.interfaces.component;

import java.io.IOException;

/**
 * 包扫描器
 *
 * @author github-kloping
 */
public interface PackageScanner {
    /**
     * scan pack
     *
     * @param classLoader
     * @param packageName
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    Class<?>[] scan(ClassLoader classLoader, String packageName) throws IOException, ClassNotFoundException;
}
