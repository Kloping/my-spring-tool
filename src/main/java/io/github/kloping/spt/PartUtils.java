package io.github.kloping.spt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class PartUtils {

    public static String getExceptionLine(Throwable e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(baos));
        return baos.toString().trim();
    }

    public static final SimpleDateFormat myFmt = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

    public static long getTimeFromNowTo(int hour, int mini, int mil) {
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

    public static int getYear() {
        String s = myFmt.format(new Date());
        return Integer.parseInt(s.substring(0, 4));
    }

    public static int getMon() {
        String s = myFmt.format(new Date());
        return Integer.parseInt(s.substring(5, 7));
    }

    public static int getDay() {
        String s = myFmt.format(new Date());
        return Integer.parseInt(s.substring(8, 10));
    }

    public static String filter(String path, Class cla) {
        if (path.equals(".") || path.equals("/") || path.equals("./") || path.trim().isEmpty()) {
            path = cla.getName().substring(0, cla.getName().indexOf("."));
        }
        return path;
    }

    public static void check(String scanPath) {
        try {
            if (PartUtils.class.getClassLoader().getResources(scanPath) == null)
                throw new RuntimeException("The name of the package you want to scan does not exist");
        } catch (IOException e) {
            e.printStackTrace();
            try {
                throw new RuntimeException("The name of the package you want to scan does not exist");
            } finally {
                System.exit(0);
            }
        }
    }

    public static Class<?>[] getAllInterfaceOrSupers(final Class<?> cla) {
        Set<Class<?>> set = new CopyOnWriteArraySet<>();
        Class cNow;
        cNow = cla;
        Class c = null;
        while ((c = cNow.getSuperclass()) != null) {
            if (c == Object.class) break;
            set.add(c);
            cNow = c;
            addAllInterfaces(set, c);
        }
        addAllInterfaces(set, cla);
        return set.toArray(new Class[0]);
    }

    private static void addAllInterfaces(Set<Class<?>> set, Class<?> cla) {
        Class[] cs = getInterfaces(cla);
        for (Class c1 : cs) {
            if (c1 == Serializable.class) continue;
            if (c1 == Comparable.class) continue;
            set.add(c1);
            addAllInterfaces(set, c1);
        }
    }

    private static Class[] getInterfaces(Class cla) {
        return cla.getInterfaces();
    }
}
