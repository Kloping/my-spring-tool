package io.github.kloping.spt;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

public final class PartUtils {

    public static String getExceptionLine(Throwable e) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        e.printStackTrace(new PrintStream(baos));
        return baos.toString().trim();
    }

    public static long getTimeFromNowTo(int hour, int mini, int mil) {
        try {
            LocalDateTime date = LocalDate.now().atTime(hour, mini, mil);
            return ChronoUnit.MILLIS.between(LocalDateTime.now(), date);
        } catch (DateTimeException e) {
            throw new IllegalArgumentException("Invalid time: " + hour + ":" + mini + ":" + mil, e);
        }
    }

    public static int getYear() {
        return LocalDate.now().getYear();
    }

    public static int getMon() {
        return LocalDate.now().getMonthValue();
    }

    public static int getDay() {
        return LocalDate.now().getDayOfMonth();
    }

    public static String filter(String path, Class cla) {
        if (path == null) path = "";
        if (path.equals(".") || path.equals("/") || path.equals("./") || path.trim().isEmpty()) {
            path = cla.getName().substring(0, cla.getName().indexOf("."));
        }
        return path;
    }

    public static void check(String scanPath) {
        try {
            if (!PartUtils.class.getClassLoader().getResources(scanPath).hasMoreElements())
                throw new RuntimeException("The name of the package you want to scan does not exist");
        } catch (IOException e) {
            throw new IllegalStateException("Unable to inspect package: " + scanPath, e);
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
