package io.github.kloping.spt.impls;

import io.github.kloping.spt.interfaces.component.ArgsManager;
import io.github.kloping.judge.Judge;
import io.github.kloping.object.ObjectUtils;

/**
 * @author github-kloping
 */
public class ArgsManagerImpl implements ArgsManager {
    private Class<?>[] classes = new Class[]{Object.class};

    @Override
    public void setArgsType(Class<?>... classes) {
        this.classes = classes;
    }

    @Override
    public boolean isLegal(Object... objects) {
        if (!Judge.isNotNull(objects)) return false;
        if (objects.length != classes.length) return false;
        for (int i = 0; i < objects.length; i++) {
            Class<?> c1 = objects[i].getClass();
            boolean k = ObjectUtils.isSuperOrInterface(c1, classes[i]);
            if (k) continue;
            else return false;
        }
        return true;
    }

    @Override
    public Class<?>[] getArgTypes() {
        return classes;
    }
}
