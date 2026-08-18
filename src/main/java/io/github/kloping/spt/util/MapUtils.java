package io.github.kloping.spt.util;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.function.Supplier;

public final class MapUtils {
    private MapUtils() {
    }

    public static <K, V> void append(Map<K, List<V>> map, K key, V value) {
        append(map, key, value, CopyOnWriteArrayList::new);
    }

    public static <K, V> void append(Map<K, List<V>> map, K key, V value, Class<? extends List> type) {
        append(map, key, value, () -> newList(type));
    }

    public static <K, V> void appendSet(Map<K, Set<V>> map, K key, V value) {
        map.computeIfAbsent(key, k -> new CopyOnWriteArraySet<>()).add(value);
    }

    public static <K, M, V> void append(Map<K, Map<M, V>> map, K key, M nestedKey, V value) {
        map.computeIfAbsent(key, k -> new java.util.concurrent.ConcurrentHashMap<>()).put(nestedKey, value);
    }

    public static <K, M, V> void append(Map<K, Map<M, V>> map, K key, M nestedKey, V value,
                                        Class<? extends Map> type) {
        map.computeIfAbsent(key, k -> newMap(type)).put(nestedKey, value);
    }

    private static <V> List<V> newList(Class<? extends List> type) {
        try { return (List<V>) type.getDeclaredConstructor().newInstance(); } catch (Exception e) { return new CopyOnWriteArrayList<>(); }
    }

    private static <K, V> Map<K, V> newMap(Class<? extends Map> type) {
        try { return (Map<K, V>) type.getDeclaredConstructor().newInstance(); } catch (Exception e) { return new java.util.concurrent.ConcurrentHashMap<>(); }
    }

    private static <K, V> void append(Map<K, List<V>> map, K key, V value, Supplier<List<V>> supplier) {
        map.computeIfAbsent(key, k -> supplier.get()).add(value);
    }
}
