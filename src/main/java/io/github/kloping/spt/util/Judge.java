package io.github.kloping.spt.util;

public final class Judge {
    private Judge() {
    }

    public static boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    public static boolean isNotEmpty(String value) {
        return !isEmpty(value);
    }

    public static boolean isNotNull(Object... values) {
        if (values == null) return false;
        for (Object value : values) {
            if (value == null) return false;
        }
        return true;
    }
}
