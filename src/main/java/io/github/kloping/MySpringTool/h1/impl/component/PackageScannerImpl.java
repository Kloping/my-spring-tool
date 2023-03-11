package io.github.kloping.MySpringTool.h1.impl.component;

import io.github.kloping.MySpringTool.interfaces.component.PackageScanner;
import io.github.kloping.file.FileUtils;
import io.github.kloping.io.ReadUtils;
import io.github.kloping.url.UrlUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * @author github-kloping
 */
public class PackageScannerImpl implements PackageScanner {
    public static final String JAR_STR = "jar";
    public static final String FILE_STR = "file";
    public static final String JAR0_STR = ".jar!";
    public static final String JAR0FILE_STR = "jar:file:";
    private boolean isRecursion = true;

    public PackageScannerImpl(boolean isRecursion) {
        this.isRecursion = isRecursion;
    }

    private List<Class<?>> classes = new ArrayList<>();

    @Override
    public List<Class<?>> getDefaultClass() {
        return classes;
    }

    @Override
    public Class<?>[] scan(Class<?> cla, ClassLoader loader, String packageName) throws Exception {
        if (!getDefaultClass().isEmpty()) {
            return getDefaultClass().toArray(new Class[0]);
        }
        Set<String> classNames = null;
        String packagePath = packageName.replace(".", "/");
        URL url = loader.getResource(packagePath);
        if (url != null) {
            String protocol = url.getProtocol().trim();
            if (FILE_STR.equals(protocol)) {
                classNames = getClassNameFromDir(url.getPath(), packageName, isRecursion);
            } else if (JAR_STR.equals(protocol)) {
                JarFile jarFile = null;
                jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                if (jarFile != null) {
                    classNames = getClassNameFromJar(jarFile.entries(), packageName, isRecursion);
                }
            }
        }
        System.out.println(classNames);
        Set<Class<?>> classes = new CopyOnWriteArraySet<>();
        for (String name : classNames) {
            Class<?> c0 = loader.loadClass(name);
            classes.add(c0);
        }
        return classes.toArray(new Class<?>[0]);
    }

    private static URL getJarFileAsJarFile(URL url) throws Exception {
        String urlPath = url.getPath();
        int i0 = urlPath.indexOf(JAR0_STR);
        String jar0 = JAR0FILE_STR + urlPath.substring(0, i0 + JAR0_STR.length() + 1);
        int i1 = jar0.length() - (JAR0FILE_STR.length());
        urlPath = urlPath.substring(i1);
        urlPath = urlPath.substring(0, urlPath.indexOf("!"));
        URL urlJar0 = new URL(jar0);
        JarFile jarFile = null;
        jarFile = ((JarURLConnection) urlJar0.openConnection()).getJarFile();
        Enumeration<JarEntry> entryEnumeration = jarFile.entries();
        JarEntry entry = jarFile.getJarEntry(urlPath);
        InputStream is = jarFile.getInputStream(entry);
        File temp = File.createTempFile("temp0", ".jar");
        FileUtils.writeBytesToFile(ReadUtils.readAll(is), temp);
        temp.deleteOnExit();
        return new URL(JAR0FILE_STR + "/" + temp.getAbsolutePath() + "!/");
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
            JarFile jarFile = null;
            if (classPath.endsWith("classes/")) {
                continue;
            } else if (classPath.startsWith("http")) {
                File temp = File.createTempFile("temp0", ".jar");
                FileUtils.writeBytesToFile(UrlUtils.getBytesFromHttpUrl(classPath.substring(0, classPath.indexOf("!"))), temp);
                jarFile = new JarFile(temp);
                if (jarFile != null) {
                    classNames.addAll(getClassNameFromJar(jarFile.entries(), packageName, isRecursion));
                }
                temp.delete();
            } else {
                jarFile = new JarFile(classPath.substring(classPath.indexOf("/")));
                if (jarFile != null) {
                    classNames.addAll(getClassNameFromJar(jarFile.entries(), packageName, isRecursion));
                }
            }
        }
        return classNames;
    }
}
