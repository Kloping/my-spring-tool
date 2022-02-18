package io.github.kloping.MySpringTool;

import io.github.kloping.MySpringTool.annotations.CommentScan;
import io.github.kloping.MySpringTool.entity.interfaces.Runner;
import io.github.kloping.MySpringTool.exceptions.NoRunException;
import io.github.kloping.MySpringTool.h1.impl.*;
import io.github.kloping.MySpringTool.h1.impl.component.*;
import io.github.kloping.MySpringTool.h1.impls.baseup.QueueExecutorImpl;
import io.github.kloping.MySpringTool.h1.impls.component.AutomaticWiringParamsH2Impl;
import io.github.kloping.MySpringTool.interfaces.*;
import io.github.kloping.MySpringTool.interfaces.component.*;

import java.io.File;
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static io.github.kloping.MySpringTool.partUtils.*;
import static io.github.kloping.common.Public.EXECUTOR_SERVICE;

/**
 * @author github-kloping
 */
public final class StarterApplication {
    public static Logger logger = new LoggerImpl();

    public static class Setting {
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

//        protected DataBaseManager dataBaseManager;

        protected Setting() {
            INSTANCE = this;
        }

        public static Setting INSTANCE = null;

        public static void Default() {
            new Setting().defaultInit();
        }

        protected void defaultInit() {
            if (logger == null)
                logger = new LoggerImpl();
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
                queueExecutor = QueueExecutorImpl.create(mainKey, poolSize, waitTime, executor);
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
                actionManager = new ActionManagerImpl(classManager);
            if (timeMethodManager == null)
                timeMethodManager = new TimeMethodManagerImpl(classManager, automaticWiringParams);
            if (httpClientManager == null)
                httpClientManager = new HttpClientManagerImpl(classManager);
            if (fieldManager == null)
                fieldManager = new FieldManagerImpl(automaticWiringValue, classManager);
            if (fieldSourceManager == null)
                fieldSourceManager = new FieldSourceManagerImpl0(classManager);

            inited = true;
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

        public FieldSourceManager getFieldSourceManager() {
            return fieldSourceManager;
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

    }

    private static int poolSize = 20;
    private static long waitTime = 12 * 1000L;
    private static String scanPath;
    private static boolean inited = false;
    private static Class<?> mainKey = Long.class;
    /**
     * started runnable
     */
    public static final List<Runnable> STARTED_RUNNABLE = new LinkedList<>();
    /**
     * on scan before
     */
    public static final List<Runnable> PRE_SCAN_RUNNABLE = new LinkedList<>();
    /**
     * on scan after
     */
    public static final List<Runnable> POST_SCAN_RUNNABLE = new LinkedList<>();

    public static void setPoolSize(int poolSize) {
        StarterApplication.poolSize = poolSize;
    }

    public static void setWaitTime(long waitTime) {
        StarterApplication.waitTime = waitTime;
    }

    /**
     * start SpringTool
     *
     * @param cla
     */
    public static void run(Class<?> cla) {
        if (cla.isAnnotationPresent(CommentScan.class)) {
            CommentScan scan = cla.getAnnotation(CommentScan.class);
            scanPath = filter(scan.path(), cla);
            check(scanPath);
            loadConf();
            work(cla);
            workAfter();
            startAfter();
        } else {
            try {
                throw new NoRunException("this class must must has @CommentScan");
            } finally {
                System.exit(0);
            }
        }
    }

    private static void startAfter() {
        for (Runnable runnable : STARTED_RUNNABLE) {
            try {
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Constraints on setting primary key parameters Cannot be multiple runs for the same primary key
     *
     * @param cla
     */
    public static void setMainKey(Class<?> cla) {
        mainKey = cla;
    }

    /**
     * set Access Types
     *
     * @param classes
     */
    public static void setAccessTypes(Class<?>... classes) {
        getInstance().argsManager.setArgsType(classes);
    }

    /**
     * matcher and run
     *
     * @param objects
     * @return
     */
    public static synchronized int executeMethod(Object... objects) {
        return getInstance().queueExecutor.queueExecute(objects[0], objects);
    }

    /**
     * on matched and run ed
     *
     * @param runner
     */
    public static void setAllAfter(Runner runner) {
        getInstance().queueExecutor.setAfter(runner);
    }

    /**
     * on run before
     *
     * @param runner
     */
    public static void setAllBefore(Runner runner) {
        getInstance().queueExecutor.setBefore(runner);
    }

    private static void workAfter() {
        Integer l = getInstance().contextManager.getContextEntity(Integer.class, "log.level");
        if (l != null) logger.setLogLevel(l.intValue());
        String format = getInstance().contextManager.getContextEntity(String.class, "out.format");
        if (format != null) logger.setFormat(format);
    }

    private static Set<String> fileSet = new CopyOnWriteArraySet<>();

    private static void loadConf() {
        for (String path : fileSet) {
            getInstance().configFileManager.load(path);
        }
    }

    private static void reloadConf() {
        for (String path : fileSet) {
            getInstance().configFileManager.load(path);
        }
    }

    /**
     * add conf file
     * and return this all conf file
     *
     * @param file
     * @return
     */
    public static Set<String> addConfFile(File file) {
        fileSet.add(file.getAbsolutePath());
        return fileSet;
    }

    /**
     * add conf file
     * and return this all conf file
     *
     * @param file
     * @return this
     */
    public static Set<String> addConfFile(String file) {
        fileSet.add(file);
        return fileSet;
    }

    private static void work(Class<?> main) {
        try {
            Object startClass = getInstance().instanceCrater.create(main, getInstance().contextManager);
            getInstance().contextManager.append(startClass);
            getInstance().classManager.add(main);
            preScan();
            for (Class<?> aClass : getInstance().packageScanner.scan(scanPath)) {
                getInstance().classManager.add(aClass);
            }
            postScan();
            logger.info("start sptool success");
        } catch (Throwable e) {
            logger.error("There is an exception=>" + e + " at " + getExceptionLine(e));
            e.printStackTrace();
        }
    }

    private static void postScan() {
        for (Runnable runnable : POST_SCAN_RUNNABLE) {
            EXECUTOR_SERVICE.submit(runnable);
        }
    }

    private static void preScan() {
        ExtensionImpl0.INSTANCE = new ExtensionImpl0();
        for (Runnable runnable : PRE_SCAN_RUNNABLE) {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.error("There is an exception=>" + e + " at " + getExceptionLine(e));
            }
        }
    }

    private static Setting getInstance() {
        synchronized (StarterApplication.class) {
            if (!inited) {
                Setting.Default();
            }
        }
        return Setting.INSTANCE;
    }
}