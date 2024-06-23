package io.github.kloping.spt.impls;

import io.github.kloping.spt.interfaces.component.ContextManager;
import io.github.kloping.map.MapUtils;
import io.github.kloping.object.ObjectUtils;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ContextManagerImpl implements ContextManager {
    private Map<Class<?>, Map<String, Object>> contexts = new ConcurrentHashMap<>();

    @Override
    public int append(Class<?> cla, Object obj, String id) {
        if (contexts.containsKey(cla)) MapUtils.append(contexts, cla, id, obj);
        else MapUtils.append(contexts, cla, id, obj, ConcurrentHashMap.class);
        return contexts.get(cla).size();
    }

    @Override
    public int append(Object obj) {
        String id = UUID.randomUUID().toString();
        return append(obj, id);
    }

    @Override
    public int append(Object obj, String id) {
        if (contexts.containsKey(obj.getClass())) MapUtils.append(contexts, obj.getClass(), id, obj);
        else MapUtils.append(contexts, obj.getClass(), id, obj, ConcurrentHashMap.class);
        return contexts.get(obj.getClass()).size();
    }

    @Override
    public <T> T getContextEntity(Class<T> cla) {
        cla = (Class<T>) ObjectUtils.baseToPack(cla);
        if (contexts.containsKey(cla)) return (T) contexts.get(cla).values().iterator().next();
        return null;
    }

    @Override
    public <T> T getContextEntity(Class<T> cla, String id) {
        cla = (Class<T>) ObjectUtils.baseToPack(cla);
        if (contexts.containsKey(cla)) {
            if (contexts.get(cla).containsKey(id)) return (T) contexts.get(cla).get(id);
        }
        return null;
    }
}
