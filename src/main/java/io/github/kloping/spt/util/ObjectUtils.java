package io.github.kloping.spt.util;

public final class ObjectUtils {
    private ObjectUtils() {
    }

    public static Class<?> baseToPack(Class<?> type) {
        if (type == null || !type.isPrimitive()) return type;
        if (type == boolean.class) return Boolean.class;
        if (type == byte.class) return Byte.class;
        if (type == short.class) return Short.class;
        if (type == int.class) return Integer.class;
        if (type == long.class) return Long.class;
        if (type == float.class) return Float.class;
        if (type == double.class) return Double.class;
        if (type == char.class) return Character.class;
        return type;
    }

    public static boolean isSuperOrInterface(Class<?> actual, Class<?> expected) {
        return expected != null && actual != null && baseToPack(expected).isAssignableFrom(baseToPack(actual));
    }

    public static Object maybeType(String value) {
        if (value == null) return null;
        if ("null".equalsIgnoreCase(value)) return null;
        if ("true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value)) return Boolean.valueOf(value);
        try { return Integer.valueOf(value); } catch (NumberFormatException ignored) { }
        try { return Long.valueOf(value); } catch (NumberFormatException ignored) { }
        try { return Double.valueOf(value); } catch (NumberFormatException ignored) { }
        if ((value.startsWith("\"") && value.endsWith("\"")) || (value.startsWith("'") && value.endsWith("'"))) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }
}
