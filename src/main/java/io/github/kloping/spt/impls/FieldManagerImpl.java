package io.github.kloping.spt.impls;

import io.github.kloping.spt.Setting;
import io.github.kloping.spt.annotations.*;
import io.github.kloping.spt.interfaces.AutomaticWiringParams;
import io.github.kloping.spt.interfaces.AutomaticWiringValue;
import io.github.kloping.spt.interfaces.Logger;
import io.github.kloping.spt.interfaces.component.ClassManager;
import io.github.kloping.spt.interfaces.component.ContextManager;
import io.github.kloping.spt.interfaces.component.FieldManager;
import io.github.kloping.spt.interfaces.component.MethodManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author github-kloping
 */
public class FieldManagerImpl implements FieldManager {

    private AutomaticWiringValue automaticWiringValue;

    public FieldManagerImpl(AutomaticWiringValue automaticWiringValue, ClassManager classManager, Setting setting) {
        this.automaticWiringValue = automaticWiringValue;
        classManager.registeredAnnotation(Entity.class, this);
        classManager.registeredAnnotation(Controller.class, this);
        classManager.registeredAnnotation(ComponentScan.class, this);
        setting.getSTARTED_RUNNABLE().add(() -> {
            workStand();
        });
        setting.getSTARTED_RUNNABLE().add(() -> {
            workStandAfter();
        });
    }

    public void workStandAfter() {
        MethodManager methodManager = contextManager.getContextEntity(MethodManager.class);
        AutomaticWiringParams automaticWiringParams = contextManager.getContextEntity(AutomaticWiringParams.class);
        for (Class claz : setClass) {
            for (Method declaredMethod : claz.getDeclaredMethods()) {
                try {
                    declaredMethod.setAccessible(true);
                    if (declaredMethod.isAnnotationPresent(AutoStandAfter.class)) {
                        Class cla = declaredMethod.getDeclaringClass();
                        Object o = contextManager.getContextEntity(cla);
                        Object[] objects = automaticWiringParams.wiring(declaredMethod, contextManager);
                        declaredMethod.invoke(o, objects);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void workStand() {
        for (Class claz : setClass) {
            for (Field declaredField : claz.getDeclaredFields()) {
                declaredField.setAccessible(true);
                try {
                    this.manager(declaredField, contextManager);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void manager(Field field, ContextManager contextManager) throws IllegalAccessException {
        Object obj = contextManager.getContextEntity(field.getDeclaringClass());
        if (field.isAnnotationPresent(AutoStand.class)) {
            automaticWiringValue.wiring(obj, field, contextManager);
            Logger logger = contextManager.getContextEntity(Logger.class);
            if (logger != null)
                logger.Log("autoStand " + field.getName() + " in " + field.getDeclaringClass().getSimpleName(), 0);
        }
    }

    private ContextManager contextManager;
    private Set<Class> setClass = new CopyOnWriteArraySet<>();

    @Override
    public void manager(Class claz, ContextManager contextManager) throws IllegalAccessException {
        setClass.add(claz);
        this.contextManager = contextManager;
    }
}
