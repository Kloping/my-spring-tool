package io.github.kloping.MySpringTool.h1.impl.component;

import io.github.kloping.MySpringTool.interfaces.component.ContextManager;
import io.github.kloping.MySpringTool.PartUtils;
import io.github.kloping.map.MapUtils;
import io.github.kloping.object.ObjectUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author github-kloping
 */
public class ContextManagerWithEIImpl implements ContextManager {
    private Map<Class<?>, Map<String, Object>> contexts = new ConcurrentHashMap<>();

    @Override
    public int append(Class<?> cla, Object obj, String id) {
        if (contexts.containsKey(cla))
            MapUtils.append(contexts, cla, id, obj);
        else
            MapUtils.append(contexts, cla, id, obj, ConcurrentHashMap.class);
        for (Class c1 : PartUtils.getAllInterfaceOrSupers(cla))
            MapUtils.append(contexts, c1, id, obj, ConcurrentHashMap.class);
        return contexts.get(cla).size();
    }

    @Override
    public int append(Object obj) {
        String id = UUID.randomUUID().toString();
        return append(obj, id);
    }

    @Override
    public int append(Object obj, String id) {
        Class cla = obj.getClass();
        if (contexts.containsKey(cla))
            MapUtils.append(contexts, cla, id, obj);
        else
            MapUtils.append(contexts, cla, id, obj, ConcurrentHashMap.class);

        for (Class c1 : PartUtils.getAllInterfaceOrSupers(cla))
            MapUtils.append(contexts, c1, id, obj, ConcurrentHashMap.class);
        return contexts.get(cla).size();
    }

    @Override
    public <T> T getContextEntity(Class<T> cla) {
        cla = (Class<T>) ObjectUtils.baseToPack(cla);
        if (contexts.containsKey(cla))
            return (T) contexts.get(cla).values().iterator().next();
        return null;
    }

    @Override
    public <T> T getContextEntity(Class<T> cla, String id) {
        cla = (Class<T>) ObjectUtils.baseToPack(cla);
        if (contexts.containsKey(cla)) {
            if (contexts.get(cla).containsKey(id))
                return (T) contexts.get(cla).get(id);
        }
        return null;
    }
}
