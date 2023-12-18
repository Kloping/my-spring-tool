package io.github.kloping.MySpringTool.h1.impls.component;

import io.github.kloping.MySpringTool.annotations.AllMess;
import io.github.kloping.MySpringTool.annotations.Param;
import io.github.kloping.MySpringTool.annotations.ReturnResult;
import io.github.kloping.MySpringTool.interfaces.AutomaticWiringParams;
import io.github.kloping.MySpringTool.interfaces.component.ContextManager;
import io.github.kloping.MySpringTool.interfaces.entitys.MatherResult;
import io.github.kloping.arr.Class2OMap;
import io.github.kloping.object.ObjectUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

import static io.github.kloping.object.ObjectUtils.baseToPack;

/**
 * @author github-kloping
 */
public class AutomaticWiringParamsH2Impl implements AutomaticWiringParams {

    @Override
    public Object[] wiring(Method method, ContextManager contextManager) throws IllegalAccessException {
        Parameter[] parameters = method.getParameters();
        if (!m2(contextManager, parameters)) {
            return null;
        }
        Object[] objects = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            objects[i] = contextManager.getContextEntity(parameters[i].getType());
        }
        return objects;
    }

    private boolean m2(ContextManager contextManager, Parameter[] parameters) {
        for (Parameter parameter : parameters) {
            if (contextManager.getContextEntity(parameter.getType()) != null) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }

    /**
     * MatherResult result = (MatherResult) objs[0];
     * Object[] objects = (Object[]) objs[1];
     *
     * @param method
     * @param objs
     * @return
     * @throws IllegalAccessException
     */
    @Override
    public Object[] wiring(Method method, Object... objs) throws IllegalAccessException {
        MatherResult result = (MatherResult) objs[0];
        List results = (List) objs[1];
        Object[] objects = (Object[]) objs[2];
        Parameter[] parameters = method.getParameters();
        Object[] ros = new Object[parameters.length];
        List<Object> usedList = new ArrayList<>();
        Set<Object> all = new HashSet<>();
        addAllObj(all, objs);
        addAllObj(all, objects);
        addAllObj(all, results);
        for (int i = 0; i < parameters.length; i++) {
            Class<?> cla = parameters[i].getType();
            if (parameters[i].isAnnotationPresent(Param.class)) {
                Param param = parameters[i].getAnnotation(Param.class);
                String r1 = result.getParams().get(param.value());
                try {
                    cla = baseToPack(cla);
                    if (cla == Long.class) {
                        ros[i] = Long.parseLong(r1);
                    } else if (cla == Integer.class) {
                        ros[i] = Integer.parseInt(r1);
                    } else if (cla == Float.class) {
                        ros[i] = Float.parseFloat(r1);
                    } else if (cla == Double.class) {
                        ros[i] = Double.parseDouble(r1);
                    } else if (cla == Boolean.class) {
                        ros[i] = Boolean.parseBoolean(r1);
                    } else {
                        ros[i] = r1;
                    }
                } catch (Exception e) {
                    ros[i] = r1;
                }
            } else if (parameters[i].isAnnotationPresent(AllMess.class)) {
                AllMess param = parameters[i].getAnnotation(AllMess.class);
                ros[i] = result.getRegx();
            } else if (parameters[i].isAnnotationPresent(ReturnResult.class)) {
                m1(results, parameters, ros, i);
            } else if (parameters[i].getType() == Class2OMap.class) {
                ros[i] = io.github.kloping.arr.Class2OMap.create(objects);
            } else {
                m0(all.toArray(), parameters[i], ros, usedList, i);
            }
        }
        return ros;
    }

    private static void addAllObj(Set<Object> all, Collection results) {
        for (Object arg : results) {
            if (arg != null) {
                if (arg.getClass().isArray()) addAllObj(all, (Object[]) arg);
                else if (arg.getClass().isAssignableFrom(Collection.class)) addAllObj(all, (Collection) arg);
                all.add(arg);
            }
        }
    }

    private static void addAllObj(Set<Object> all, Object... args) {
        for (Object arg : args) {
            if (arg != null) {
                if (arg.getClass().isArray()) addAllObj(all, (Object[]) arg);
                else if (arg.getClass().isAssignableFrom(Collection.class)) addAllObj(all, (Collection) arg);
                all.add(arg);
            }
        }
    }

    private void m1(List results, Parameter[] parameters, Object[] ros, int i) {
        for (Object o : results) {
            if (parameters[i].getType().isAssignableFrom(o.getClass())) {
                ros[i] = o;
                results.remove(o);
                return;
            }
        }
    }

    private void m0(Object[] objects, Parameter parameter, Object[] ros, List<Object> list, int i) {
        for (Object o : objects) {
            Class pc = ObjectUtils.baseToPack(parameter.getType());
            if (ObjectUtils.isSuperOrInterface(o.getClass(), pc)) {
                if (!list.contains(o)) {
                    ros[i] = o;
                    list.add(o);
                    return;
                }
            }
        }
    }
}
