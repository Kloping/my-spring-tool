package io.github.kloping.MySpringTool.h1.impl.component;

import io.github.kloping.MySpringTool.StarterApplication;
import io.github.kloping.MySpringTool.annotations.Action;
import io.github.kloping.MySpringTool.annotations.After;
import io.github.kloping.MySpringTool.annotations.Before;
import io.github.kloping.MySpringTool.annotations.Controller;
import io.github.kloping.MySpringTool.interfaces.component.ActionManager;
import io.github.kloping.MySpringTool.interfaces.component.ClassManager;
import io.github.kloping.MySpringTool.interfaces.component.ContextManager;
import io.github.kloping.MySpringTool.interfaces.entitys.MatherResult;
import io.github.kloping.map.MapUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static io.github.kloping.judge.Judge.isNotNull;

/**
 * @author github-kloping
 */
public class ActionManagerImpl implements ActionManager {

    public ActionManagerImpl(ClassManager classManager) {
        classManager.registeredAnnotation(Controller.class, this);
    }

    public static final Pattern PATTERN0 = Pattern.compile("<.*>");
    public static final Pattern PATTERN1 = Pattern.compile("<.+=>.+>");
    public Map<String, String> histIndexes = new HashMap<>();

    @Override
    public synchronized MatherResult mather(String regx) {
        if (regx == null || regx.trim().isEmpty()) return null;
        if (indexMap.isEmpty()) makeIndex();
        if (maps.containsKey(regx)) {
            Set<Method> set = maps.get(regx);
            MatherResult mr = new MatherResult(regx, regx, set.toArray(new Method[0]));
            return mr;
        } else {
            if (histIndexes.containsKey(regx)) {
                MatherResult r = null;
                try {
                    r = mr(regx, histIndexes.get(regx));
                    if (r != null) return r;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            for (char c : getShortestSortedChars(regx)) {
                if (indexMap.containsKey(c)) {
                    for (String s : indexMap.get(c)) {
                        MatherResult r = null;
                        try {
                            r = mr(regx, s);
                        } catch (Exception e) {
                            continue;
                        }
                        if (r != null) {
                            histIndexes.put(regx, s);
                            return r;
                        }
                    }
                }
            }

        }
        return null;
    }

    private List<Character> getShortestSortedChars(String s0) {
        Map<Character, Integer> c2n = new HashMap<>();
        for (char c : s0.toCharArray()) {
            if (indexMap.containsKey(c)) {
                int n = indexMap.get(c).size();
                c2n.put(c, n);
            }
        }
        List<Map.Entry<Character, Integer>> ls = new LinkedList<>(c2n.entrySet());
        Collections.sort(ls, new Comparator<Map.Entry<Character, Integer>>() {
            @Override
            public int compare(Map.Entry<Character, Integer> o1, Map.Entry<Character, Integer> o2) {
                return o1.getValue() - o2.getValue();
            }
        });
        List<Character> endLs = new LinkedList<>();
        for (Map.Entry<Character, Integer> l : ls) {
            endLs.add(l.getKey());
        }
        return endLs;
    }

    private MatherResult mr(String regx, String s) throws Exception {
        if (regx.matches(s)) {
            Set<Method> set = maps.get(s);
            MatherResult mr = new MatherResult(s, regx, set.toArray(new Method[0]));
            return mr;
        } else {
            Matcher matcher = PATTERN0.matcher(s);
            if (matcher.find()) {
                String s1 = matcher.group();
                String s2 = s1.substring(1, s1.length() - 1);
                String regxNow = s.replace(s1, s2);
                if (regx.matches(regxNow)) {
                    Set<Method> set = maps.get(s);
                    MatherResult mr = new MatherResult(s, regx, set.toArray(new Method[0]));
                    return mr;
                }
            }
            matcher = PATTERN1.matcher(s);
            if (matcher.find()) {
                String s1 = matcher.group();
                String s2 = s1.substring(1, s1.length() - 1);
                String[] ss = s2.split("=>");
                String regxNow = s.replace(s1, ss[0]);
                if (regx.matches(regxNow)) {
                    String s4 = m1(ss[0]);
                    String[] ss2 = regxNow.split(s4);
                    String nowRegx = regx;
                    String m1 = regxNow.replace(ss[0], "");
                    nowRegx = nowRegx.replaceFirst(m1, "");
                    Set<Method> set = maps.get(s);
                    MatherResult mr = new MatherResult(s, regx, set.toArray(new Method[0]));
                    if (nowRegx.matches(ss[0])) {
                        mr.getParams().put(ss[1], nowRegx);
                    }
                    return mr;
                }
            }
        }
        return null;
    }

    @Override
    public Class<?>[] getAll() {
        return classSet.toArray(new Class[0]);
    }

    private List<Character> csO = new LinkedList<>();

    {
        csO.add('<');
        csO.add('>');
        csO.add('.');
        csO.add('?');
        csO.add('=');
        csO.add('+');
        csO.add('*');
    }

    public void makeIndex() {
        indexMap.clear();
        for (String k : maps.keySet()) {
            for (Character c : k.toCharArray()) {
                if (csO.contains(c)) continue;
                MapUtils.append(indexMap, c, k);
            }
        }
    }

    private Map<Character, List<String>> indexMap = new HashMap<>();

    private Map<String, Set<Method>> maps = new ConcurrentHashMap<>();

    private Set<Class<?>> classSet = new CopyOnWriteArraySet<>();

    @Override
    public void manager(Method method, ContextManager contextManager) throws IllegalAccessException, InvocationTargetException {
        this.manager(contextManager.getContextEntity(method.getDeclaringClass()));
    }

    @Override
    public void manager(Object obj) throws IllegalAccessException, InvocationTargetException {
        if (obj == null) return;
        if (obj.getClass() == null) return;
        if (obj.getClass() == Class.class) return;
        if (!classSet.add(obj.getClass())) return;
        Method before = null;
        Method after = null;
        for (Method declaredMethod : obj.getClass().getDeclaredMethods()) {
            if (declaredMethod.isAnnotationPresent(Before.class)) {
                declaredMethod.setAccessible(true);
                before = declaredMethod;
                StarterApplication.logger.Log("new before " + declaredMethod.getName() + " from "
                        + declaredMethod.getDeclaringClass().getSimpleName(), 0);
            } else if (declaredMethod.isAnnotationPresent(After.class)) {
                declaredMethod.setAccessible(true);
                after = declaredMethod;
                StarterApplication.logger.Log("new after  " + declaredMethod.getName() + " from "
                        + declaredMethod.getDeclaringClass().getSimpleName(), 0);
            }
        }
        for (Method declaredMethod : obj.getClass().getDeclaredMethods()) {
            if (!declaredMethod.isAnnotationPresent(Action.class)) continue;
            declaredMethod.setAccessible(true);
            Action action = declaredMethod.getDeclaredAnnotation(Action.class);
            Set<String> actionsStr = new LinkedHashSet<>();
            if (!action.value().trim().isEmpty())
                actionsStr.add(action.value());
            for (String ac : action.otherName())
                if (!ac.trim().isEmpty())
                    actionsStr.add(ac);

            for (String a : actionsStr) {
                if (before != null)
                    append(maps, a, before);
                append(maps, a, declaredMethod);
                if (after != null)
                    append(maps, a, after);
            }

            StarterApplication.logger.Log("new action  " + declaredMethod.getName() + " from "
                    + declaredMethod.getDeclaringClass().getSimpleName(), 0);
        }
    }

    public static <K, V> void append(Map<K, Set<V>> map, K k, V v) {
        if (!isNotNull(map, k, v)) return;
        Set<V> set = map.get(k);
        if (set == null) set = new CopyOnWriteArraySet<>();
        set.add(v);
        map.put(k, set);
    }

    private static List<Character> need1 = new LinkedList<>();

    static {
        need1.add('.');
        need1.add('\\');
        need1.add('[');
        need1.add(']');
        need1.add('+');
        need1.add('?');
        need1.add('*');
        need1.add('^');
        need1.add('{');
    }

    public static String m1(String m1) {
        StringBuilder sb = new StringBuilder();
        for (char c : m1.toCharArray()) {
            if (need1.contains(c))
                sb.append("\\");
            sb.append(c);
        }
        return sb.toString();
    }

    @Override
    public synchronized void replaceAction(String oStr, String nowStr) {
        if (maps.containsKey(oStr)) {
            Set<Method> set = maps.get(oStr);
            maps.put(nowStr, set);
            maps.remove(oStr);
            for (char c : oStr.toCharArray()) {
                indexMap.remove(c);
            }
            for (char c : nowStr.toCharArray()) {
                MapUtils.append(indexMap, c, nowStr);
            }
            histIndexes.clear();
        }
    }
}






