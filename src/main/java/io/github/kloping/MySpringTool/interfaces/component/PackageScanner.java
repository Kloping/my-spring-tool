package io.github.kloping.MySpringTool.interfaces.component;

import java.io.IOException;

/**
 * 包扫描器
 */
public interface PackageScanner {
    Class<?>[] scan(String packageName) throws IOException, ClassNotFoundException;
}
