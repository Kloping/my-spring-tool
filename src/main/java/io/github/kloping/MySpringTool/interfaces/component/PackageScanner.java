package io.github.kloping.MySpringTool.interfaces.component;

import java.io.IOException;

public interface PackageScanner {
    Class<?>[] scan(String packageName) throws IOException, ClassNotFoundException;
}
