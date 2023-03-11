package io.github.kloping.MySpringTool.interfaces.component;

import java.io.IOException;
import java.util.List;

/**
 * 包扫描器
 *
 * @author github-kloping
 */
public interface PackageScanner {
    /**
     * scan pack
     *
     * @param cla
     * @param classLoader
     * @param packageName
     * @return
     * @throws IOException
     * @throws ClassNotFoundException
     */
    Class<?>[] scan(Class<?> cla, ClassLoader classLoader, String packageName) throws Exception;

    /**
     * 获取预设 classes 若不为空则跳过scan
     *
     * @return
     */
    List<Class<?>> getDefaultClass();
}
