package io.github.kloping.spt.impls;

import io.github.kloping.spt.PartUtils;
import io.github.kloping.spt.annotations.Param;
import lombok.extern.slf4j.Slf4j;
import io.github.kloping.spt.interfaces.component.ContextManager;
import io.github.kloping.spt.interfaces.component.InstanceCrater;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author github-kloping
 */
@Slf4j
public class InstanceCraterImpl implements InstanceCrater {
    @Override
    public <T> T create(Class<T> cla, ContextManager contextManager) {
        Constructor<?>[] constructors = getConstructors(cla);
        T t = null;
        for (Constructor con : constructors) {
            try {
                con.setAccessible(true);
                Parameter[] parameters = con.getParameters();
                if (parameters.length == 0) {
                    t = (T) con.newInstance();
                } else {
                    Object[] args = new Object[parameters.length];
                    int index = 0;
                    for (Parameter parameter : parameters) {
                        String id = null;
                        if (parameter.isAnnotationPresent(Param.class)) {
                            Param p = parameter.getDeclaredAnnotation(Param.class);
                            id = p.value();
                        }
                        args[index++] = contextManager.getContextEntity(parameter.getType(), id);
                    }
                    t = (T) con.newInstance(args);
                }
                break;
            } catch (InvocationTargetException ex) {
                Throwable e = ex.getTargetException();
                if (e instanceof RuntimeException) {
                    String msg = e.getMessage() + " at create bean " + cla.getSimpleName() + " parameters " + Arrays.toString(con.getParameters());
                    log.error(msg, e);
                    io.github.kloping.spt.annotations.Constructor constructor = con.getDeclaredAnnotation(io.github.kloping.spt.annotations.Constructor.class);
                    if (constructor != null && constructor.value() != 0) break;
                    else continue;
                } else {
                    log.error(PartUtils.getExceptionLine(e), e);
                }
            } catch (InstantiationException | IllegalAccessException e) {
                log.error(PartUtils.getExceptionLine(e), e);
            }
        }
        return t;
    }

    private <T> Constructor<?>[] getConstructors(Class<T> cla) {
        //是否需要未注解的构造
        boolean k = true;
        Constructor<?>[] constructors = cla.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            constructor.setAccessible(true);
            if (constructor.isAnnotationPresent(io.github.kloping.spt.annotations.Constructor.class)) {
                k = false;
            }
        }
        if (k) return constructors;
        List<Constructor> list = new ArrayList<>();
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(io.github.kloping.spt.annotations.Constructor.class)) {
                list.add(constructor);
            }
        }
        return list.toArray(new Constructor[0]);
    }
}
