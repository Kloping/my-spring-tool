package io.github.kloping.spt.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.lang.reflect.Field;

public final class HmlObject {
    private final Map<String, Object> entry = new LinkedHashMap<>();
    private String typeName;

    public static HmlObject parse(String text) {
        String[] lines = text == null ? new String[0] : text.split("\\R", -1);
        return parse(lines, new int[]{0}, 0);
    }

    public Map<String, Object> getEntry() { return entry; }

    private static HmlObject parse(String[] lines, int[] cursor, int indent) {
        HmlObject result = new HmlObject();
        while (cursor[0] < lines.length) {
            String raw = lines[cursor[0]];
            String trimmed = raw.trim();
            int currentIndent = raw.length() - raw.replaceFirst("^\\s*", "").length();
            if (trimmed.isEmpty() || trimmed.startsWith("//") || trimmed.startsWith("#")) {
                cursor[0]++;
                continue;
            }
            if (currentIndent < indent) break;
            if (currentIndent > indent) {
                cursor[0]++;
                continue;
            }
            if (trimmed.startsWith("->")) {
                result.typeName = trimmed.substring(2).trim();
                cursor[0]++;
                if (cursor[0] < lines.length && indentation(lines[cursor[0]]) > indent) {
                    HmlObject fields = parse(lines, cursor, indentation(lines[cursor[0]]));
                    result.entry.putAll(fields.entry);
                }
                continue;
            }
            int split = trimmed.indexOf(':');
            if (split < 0) split = trimmed.indexOf('=');
            if (split < 1) {
                cursor[0]++;
                continue;
            }
            String key = trimmed.substring(0, split).trim();
            String value = trimmed.substring(split + 1).trim();
            cursor[0]++;
            if (value.isEmpty() && cursor[0] < lines.length) {
                int childIndent = indentation(lines[cursor[0]]);
                if (childIndent > indent) {
                    result.entry.put(key, parse(lines, cursor, childIndent));
                    continue;
                }
            }
            if (value.startsWith("->") && cursor[0] < lines.length && indentation(lines[cursor[0]]) > indent) {
                HmlObject child = parse(lines, cursor, indentation(lines[cursor[0]]));
                child.typeName = value.substring(2).trim();
                result.entry.put(key, child);
            } else {
                result.entry.put(key, ObjectUtils.maybeType(unquote(value)));
            }
        }
        return result;
    }

    private static int indentation(String value) {
        return value.length() - value.replaceFirst("^\\s*", "").length();
    }

    private static String unquote(String value) {
        if (value.length() >= 2 && value.startsWith("\"") && value.endsWith("\"")) {
            return value.substring(1, value.length() - 1);
        }
        return value;
    }

    public Object toJavaObject() {
        if (typeName == null || typeName.isEmpty()) return entry;
        try {
            Class<?> type = Class.forName(typeName);
            Object result = type.getDeclaredConstructor().newInstance();
            for (Map.Entry<String, Object> item : entry.entrySet()) {
                Field field;
                try { field = type.getDeclaredField(item.getKey()); }
                catch (NoSuchFieldException ignored) { continue; }
                field.setAccessible(true);
                Object value = item.getValue() instanceof HmlObject
                        ? ((HmlObject) item.getValue()).toJavaObject() : item.getValue();
                if (value != null && !field.getType().isAssignableFrom(value.getClass())) {
                    value = convert(field.getType(), value);
                }
                field.set(result, value);
            }
            return result;
        } catch (ReflectiveOperationException e) {
            throw new IllegalArgumentException("Unable to parse HML object: " + typeName, e);
        }
    }

    private static Object convert(Class<?> type, Object value) {
        String text = String.valueOf(value);
        if (type == String.class) return text;
        if (type == int.class || type == Integer.class) return Integer.valueOf(text);
        if (type == long.class || type == Long.class) return Long.valueOf(text);
        if (type == boolean.class || type == Boolean.class) return Boolean.valueOf(text);
        if (type == double.class || type == Double.class) return Double.valueOf(text);
        if (type == float.class || type == Float.class) return Float.valueOf(text);
        return value;
    }
}
