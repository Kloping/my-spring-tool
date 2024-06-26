package io.github.kloping.spt;

import io.github.kloping.spt.impls.*;
import io.github.kloping.spt.impls.QueueExecutorImpl;
import io.github.kloping.spt.impls.baseup.AutomaticWiringParamsH2Impl;
import io.github.kloping.spt.interfaces.AutomaticWiringParams;
import io.github.kloping.spt.interfaces.AutomaticWiringValue;
import io.github.kloping.spt.interfaces.Executor;
import io.github.kloping.spt.interfaces.QueueExecutor;
import io.github.kloping.spt.interfaces.component.*;

import java.lang.reflect.Field;
import java.util.List;

/**
 * @author github-kloping
 */
public abstract class Setting {
    protected ContextManager contextManager;
    protected PackageScanner packageScanner;
    protected AutomaticWiringParams automaticWiringParams;
    protected AutomaticWiringValue automaticWiringValue;
    protected ArgsManager argsManager;
    protected InstanceCrater instanceCrater;
    protected ActionManager actionManager;
    protected MethodManager methodManager;
    protected FieldManager fieldManager;
    protected ClassManager classManager;
    protected ConfigFileManager configFileManager;
    protected Executor executor;
    protected QueueExecutor queueExecutor;
    protected TimeMethodManager timeMethodManager;
    protected HttpClientManager httpClientManager;
    protected FieldSourceManager fieldSourceManager;

    protected Setting() {
    }

    protected Setting defaultInit(Class mainKey, Integer poolSize, Long waitTime) {
        if (contextManager == null)
            contextManager = new ContextManagerWithEIImpl();
        if (configFileManager == null)
            configFileManager = new ConfigFileManagerImpl(contextManager);
        if (automaticWiringParams == null)
            automaticWiringParams = new AutomaticWiringParamsH2Impl();
        if (automaticWiringValue == null)
            automaticWiringValue = new AutomaticWiringValueImpl();
        if (instanceCrater == null)
            instanceCrater = new InstanceCraterImpl();
        if (executor == null)
            executor = new ExecutorNowImpl();
        if (queueExecutor == null)
            queueExecutor = QueueExecutorImpl.create(mainKey, poolSize, waitTime, executor, this);
        if (argsManager == null)
            argsManager = new ArgsManagerImpl();
        if (packageScanner == null)
            packageScanner = new PackageScannerImpl(true);
        if (classManager == null)
            classManager = new ClassManagerImpl(
                    instanceCrater, contextManager, automaticWiringParams, actionManager
            );
        if (methodManager == null)
            methodManager = new MethodManagerImpl(automaticWiringParams, classManager);
        if (actionManager == null)
            actionManager = new ActionManagerImpl(classManager,Setting.this);
        if (timeMethodManager == null)
            timeMethodManager = new TimeMethodManagerImpl(this, classManager, automaticWiringParams);
        if (httpClientManager == null)
            httpClientManager = new HttpClientManagerImpl(this, classManager);
        if (fieldManager == null)
            fieldManager = new FieldManagerImpl(automaticWiringValue, classManager, this);
        if (fieldSourceManager == null)
            fieldSourceManager = new FieldSourceManagerImpl0(classManager);

        Field[] fields = Setting.class.getDeclaredFields();
        for (Field field : fields) {
            Object o = null;
            try {
                o = field.get(this);
            } catch (Exception e) {
                continue;
            }
            if (o != null) {
                contextManager.append(o);
            }
        }
        return this;
    }

    public ContextManager getContextManager() {
        return contextManager;
    }

    public PackageScanner getPackageScanner() {
        return packageScanner;
    }

    public AutomaticWiringParams getAutomaticWiringParams() {
        return automaticWiringParams;
    }

    public AutomaticWiringValue getAutomaticWiringValue() {
        return automaticWiringValue;
    }

    public ArgsManager getArgsManager() {
        return argsManager;
    }

    public InstanceCrater getInstanceCrater() {
        return instanceCrater;
    }

    public ActionManager getActionManager() {
        return actionManager;
    }

    public MethodManager getMethodManager() {
        return methodManager;
    }

    public FieldManager getFieldManager() {
        return fieldManager;
    }

    public ClassManager getClassManager() {
        return classManager;
    }

    public ConfigFileManager getConfigFileManager() {
        return configFileManager;
    }

    public Executor getExecutor() {
        return executor;
    }

    public QueueExecutor getQueueExecutor() {
        return queueExecutor;
    }

    public TimeMethodManager getTimeMethodManager() {
        return timeMethodManager;
    }

    public HttpClientManager getHttpClientManager() {
        return httpClientManager;
    }

    public FieldSourceManager getFieldSourceManager() {
        return fieldSourceManager;
    }

    public abstract List<Runnable> getSTARTED_RUNNABLE();

    public abstract List<Runnable> getPRE_SCAN_RUNNABLE();

    public abstract List<Runnable> getPOST_SCAN_RUNNABLE();
}