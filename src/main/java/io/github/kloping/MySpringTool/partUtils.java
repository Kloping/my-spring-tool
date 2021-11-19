package io.github.kloping.MySpringTool;

import io.github.kloping.MySpringTool.exceptions.NoRunException;
import io.github.kloping.map.MapUtils;
import io.github.kloping.object.ObjectUtils;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import static io.github.kloping.MySpringTool.Starter.Log;

final class partUtils {

    static Set<Class<?>> getClassName(String packageName, boolean isRecursion) {
        Set<String> classNames = null;
        ClassLoader loader = Starter.class.getClassLoader();
        String packagePath = packageName.replace(".", "/");
        URL url = loader.getResource(packagePath);
        if (url != null) {
            String protocol = url.getProtocol().trim();
            if (protocol.equals("file")) {
                classNames = getClassNameFromDir(url.getPath(), packageName, isRecursion);
            } else if (protocol.equals("jar")) {
                JarFile jarFile = null;
                try {
                    jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                } catch (Exception e) {
                    Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);
                }
                if (jarFile != null) {
                    classNames = getClassNameFromJar(jarFile.entries(), packageName, isRecursion);
                }
            }
        } else {
            classNames = getClassNameFromJars(((URLClassLoader) loader).getURLs(), packageName, isRecursion);
        }
        System.out.println(classNames);
        Set<Class<?>> classes = new CopyOnWriteArraySet<>();
        for (String name : classNames) {
            try {
                Class<?> cla = loader.loadClass(name);
                classes.add(cla);
            } catch (ClassNotFoundException e) {
                Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);
            }
        }
        return classes;
    }

    static Set<String> getClassNameFromDir(String filePath, String packageName, boolean isRecursion) {
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

    static Set<String> getClassNameFromJar(Enumeration<JarEntry> jarEntries, String packageName, boolean isRecursion) {
        return getResource0(jarEntries, packageName, isRecursion);
    }

    static Set<String> getResource0(Enumeration<JarEntry> jarEntries, String packageName, boolean isRecursion) {
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

    static Set<String> getClassNameFromJars(URL[] urls, String packageName, boolean isRecursion) {
        Set<String> classNames = new HashSet<>();
        for (int i = 0; i < urls.length; i++) {
            String classPath = urls[i].getPath();
            if (classPath.endsWith("classes/")) {
                continue;
            }
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(classPath.substring(classPath.indexOf("/")));
            } catch (IOException e) {
                Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);
            }
            if (jarFile != null) {
                classNames.addAll(getClassNameFromJar(jarFile.entries(), packageName, isRecursion));
            }
        }
        return classNames;
    }

    static String getExceptionLine(Throwable e) {
        try {
            Method method = Throwable.class.getDeclaredMethod("getOurStackTrace");
            method.setAccessible(true);
            Object[] objects = (Object[]) method.invoke(e);
            StringBuilder sb = new StringBuilder("\r\n");
            for (Object o : objects) {
                sb.append(" at ").append(o.toString()).append("\r\n\t");
            }
            return sb.toString();
        } catch (Exception e1) {
            return "??";
        }
    }

    static void getTargetException(InvocationTargetException e) {
        InvocationTargetException ite = e;
        if (ite.getTargetException().getClass() == NoRunException.class) {
            NoRunException exception = (NoRunException) ite.getTargetException();
            Log("抛出 不运行异常(throw NuRunException): " + exception.getMessage(), 2);
        } else {
            Log("存在映射一个异常(Has a Invoke Exception)=>" + ite.getTargetException() + " at " + getExceptionLine(ite.getTargetException()), -1);
        }
    }

    static Class<?>[] ObjectsToClasses(Object... objs) {
        Class<?>[] classes = new Class[objs.length];
        for (int i = 0; i < objs.length; i++) {
            classes[i] = objs[i].getClass();
        }
        return classes;
    }

    static final SimpleDateFormat myFmt = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    static long getTimeFromNowTo(int hour, int mini, int mil) {
        Date date = null;
        try {
            String p1 = String.format("%s-%s-%s-%s-%s-%s", getYear(), getMon(), getDay(), hour, mini, mil);
            date = myFmt.parse(p1);
        } catch (Exception e) {
        }
        long millis = date.getTime();
        long now = System.currentTimeMillis();
        return millis - now;
    }

    static int getYear() {
        String s = myFmt.format(new Date());
        return Integer.parseInt(s.substring(0, 4));
    }

    static int getMon() {
        String s = myFmt.format(new Date());
        return Integer.parseInt(s.substring(5, 7));
    }

    static int getDay() {
        String s = myFmt.format(new Date());
        return Integer.parseInt(s.substring(8, 10));
    }

    public static <K, V> Map.Entry<K, V> getEntry(K k, V v) {
        Map.Entry<K, V> entry = new Map.Entry<K, V>() {
            final K _k = k;
            V _v = v;

            @Override
            public K getKey() {
                return _k;
            }

            @Override
            public V getValue() {
                return _v;
            }

            @Override
            public V setValue(V v) {
                V v1 = _v;
                this._v = v;
                return v1;
            }
        };
        return entry;
    }

    static boolean superOrImpl(final Class<?> father, final Class<?> son) {
        try {
            if (father2son.get(father).contains(son)) {
                Log("从历史匹配知道 " + father + " 匹配与=>" + son, 0);
                return true;
            }
        } catch (Exception e) {
        }
        boolean k = ObjectUtils.isSuper(son, father);
        if (!k) k = ObjectUtils.isInterface(son, father);
        if (k) appendFather2Son(father, son);
        return k;
    }

    static void appendFather2Son(Class<?> father, Class<?> son) {
        MapUtils.append(father2son, father, son, CopyOnWriteArrayList.class);
    }

    private static final Map<Class<?>, List<Class>> father2son = new ConcurrentHashMap<>();

    static boolean isInterfaces(Class<?>[] classes1, Class<?> cla) {
        if (classes1 == null || classes1.length == 0) return false;
        for (Class c : classes1) {
            if (c == cla) return true;
            else {
                if (isInterfaces(c.getInterfaces(), cla)) return true;
            }
        }
        return false;
    }
}
