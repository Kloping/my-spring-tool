package io.github.kloping.MySpringTool.h1.impl.component;

import io.github.kloping.MySpringTool.interfaces.component.PackageScanner;

import java.io.File;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class PackageScannerImpl implements PackageScanner {
    private boolean isRecursion = true;

    public PackageScannerImpl(boolean isRecursion) {
        this.isRecursion = isRecursion;
    }

    @Override
    public Class<?>[] scan(String packageName) throws IOException, ClassNotFoundException {
        Set<String> classNames = null;
        ClassLoader loader = this.getClass().getClassLoader();
        String packagePath = packageName.replace(".", "/");
        URL url = loader.getResource(packagePath);
        if (url != null) {
            String protocol = url.getProtocol().trim();
            if (protocol.equals("file")) {
                classNames = getClassNameFromDir(url.getPath(), packageName, isRecursion);
            } else if (protocol.equals("jar")) {
                JarFile jarFile = null;
                jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                if (jarFile != null) {
                    classNames = getClassNameFromJar(jarFile.entries(), packageName, isRecursion);
                }
            }
        } else {
            classNames = getClassNameFromJars(((URLClassLoader) loader).getURLs(), packageName, isRecursion);
        }
        Set<Class<?>> classes = new CopyOnWriteArraySet<>();
        for (String name : classNames) {
            Class<?> cla = loader.loadClass(name);
            classes.add(cla);
        }
        return classes.toArray(new Class<?>[0]);
    }

    private Set<String> getClassNameFromDir(String filePath, String packageName, boolean isRecursion) {
        Set<String> className = new HashSet<>();
        File file = new File(filePath);
        File[] files = file.listFiles();
        for (File childFile : files) {
            if (childFile.isDirectory()) {
                if (isRecursion) {
                    className.addAll(getClassNameFromDir(childFile.getPath(), packageName + "." + childFile.getName(), isRecursion));
                }
            } else {
                String fileName = childFile.getName();
                if (fileName.endsWith(".class") && !fileName.contains("$")) {
                    className.add(packageName + "." + fileName.replace(".class", ""));
                }
            }
        }
        return className;
    }

    private Set<String> getClassNameFromJar(Enumeration<JarEntry> jarEntries, String packageName, boolean isRecursion) {
        return getResource0(jarEntries, packageName, isRecursion);
    }

    private Set<String> getResource0(Enumeration<JarEntry> jarEntries, String packageName, boolean isRecursion) {
        Set<String> classNames = new HashSet();
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            if (!jarEntry.isDirectory()) {
                String entryName = jarEntry.getName().replaceAll("/", ".");
                if (entryName.endsWith(".class") && !entryName.contains("$") && entryName.startsWith(packageName)) {
                    entryName = entryName.replace(".class", "");
                    if (isRecursion) {
                        classNames.add(entryName);
                    } else if (!entryName.replace(packageName + ".", "").contains(".")) {
                        classNames.add(entryName);
                    }
                }
            }
        }
        return classNames;
    }

    private Set<String> getClassNameFromJars(URL[] urls, String packageName, boolean isRecursion) throws IOException {
        Set<String> classNames = new HashSet<>();
        for (int i = 0; i < urls.length; i++) {
            String classPath = urls[i].getPath();
            if (classPath.endsWith("classes/")) {
                continue;
            }
            JarFile jarFile = null;
            jarFile = new JarFile(classPath.substring(classPath.indexOf("/")));
            if (jarFile != null) {
                classNames.addAll(getClassNameFromJar(jarFile.entries(), packageName, isRecursion));
            }
        }
        return classNames;
    }
}
