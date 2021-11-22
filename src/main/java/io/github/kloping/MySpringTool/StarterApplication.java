package io.github.kloping.MySpringTool;

import io.github.kloping.MySpringTool.annotations.CommentScan;
import io.github.kloping.MySpringTool.entity.Runner;
import io.github.kloping.MySpringTool.exceptions.NoRunException;
import io.github.kloping.MySpringTool.h1.impl.AutomaticWiringValueImpl;
import io.github.kloping.MySpringTool.h1.impl.ConfigFileManagerImpl;
import io.github.kloping.MySpringTool.h1.impl.InstanceCraterImpl;
import io.github.kloping.MySpringTool.h1.impl.LoggerImpl;
import io.github.kloping.MySpringTool.h1.impl.component.*;
import io.github.kloping.MySpringTool.h1.impls.baseup.QueueExecutorImpl;
import io.github.kloping.MySpringTool.h1.impls.component.AutomaticWiringParamsH2Impl;
import io.github.kloping.MySpringTool.interfaces.*;
import io.github.kloping.MySpringTool.interfaces.component.*;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static io.github.kloping.MySpringTool.StarterApplication.Setting.INSTANCE;
import static io.github.kloping.MySpringTool.partUtils.*;

public final class StarterApplication {
    public static Logger logger;

    public static final class Setting {
        private ContextManager contextManager;
        private PackageScanner packageScanner;
        private AutomaticWiringParams automaticWiringParams;
        private AutomaticWiringValue automaticWiringValue;
        private ArgsManager argsManager;
        private InstanceCrater instanceCrater;
        private ActionManager actionManager;
        private MethodManager methodManager;
        private FieldManager fieldManager;
        private ClassManager classManager;
        private ConfigFileManager configFileManager;
        private Executor executor;
        private QueueExecutor queueExecutor;
        private TimeMethodManager timeMethodManager;
        private HttpClientManager httpClientManager;

        private Setting() {
            if (INSTANCE != null) throw new RuntimeException("cannot create multiple Setting instances");
            else defaultInit();
        }

        public static final Setting INSTANCE = new Setting();

        private void defaultInit() {
            logger = new LoggerImpl();
            contextManager = new ContextManagerWithEIImpl();
            automaticWiringParams = new AutomaticWiringParamsH2Impl();
            automaticWiringValue = new AutomaticWiringValueImpl();
            instanceCrater = new InstanceCraterImpl();
            executor = new ExecutorNowImpl();

            queueExecutor = QueueExecutorImpl.create(mainKey, poolSize, waitTime, executor);

            argsManager = new ArgsManagerImpl();

            packageScanner = new PackageScannerImpl(true);

            configFileManager = new ConfigFileManagerImpl(contextManager);

            classManager = new ClassManagerImpl(
                    instanceCrater, contextManager, automaticWiringParams, actionManager
            );

            methodManager = new MethodManagerImpl(automaticWiringParams, classManager);
            actionManager = new ActionManagerImpl(classManager);
            timeMethodManager = new TimeMethodManagerImpl(classManager, automaticWiringParams);
            httpClientManager = new HttpClientManagerImpl(classManager);
            fieldManager = new FieldManagerImpl(automaticWiringValue, classManager);

            inited = true;
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
    }

    private static int poolSize = 20;
    private static long waitTime = 12 * 1000L;
    private static String scanPath;
    private static boolean inited = false;
    private static Class<?> mainKey = Long.class;

    /**
     * 启动 SpringTool
     *
     * @param cla
     */
    public static void run(Class<?> cla) {
        if (!inited) {
            Setting.INSTANCE.defaultInit();
        }
        if (cla.isAnnotationPresent(CommentScan.class)) {
            CommentScan scan = cla.getAnnotation(CommentScan.class);
            scanPath = filter(scan.path(), cla);
            check(scanPath);
            loadConf();
            work(cla);
            workAfter();
            startAfter();
        } else {
            throw new NoRunException("此类上必须存在 CommentScan 注解 (class must has @interface CommentScan )");
        }
    }

    /**
     * 启动后 runnable
     */
    public static List<Runnable> startAfterRunnerList = new LinkedList<>();

    private static void startAfter() {
        for (Runnable runnable : startAfterRunnerList) {
            try {
                runnable.run();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void setMainKey(Class<?> cla) {
        mainKey = cla;
    }

    public static void setAccessTypes(Class<?>... classes) {
        INSTANCE.argsManager.setArgsType(classes);
    }

    public static synchronized int ExecuteMethod(Object... objects) {
        return INSTANCE.queueExecutor.QueueExecute(objects[0], objects);
    }

    public static void setAllAfter(Runner runner) {
        INSTANCE.queueExecutor.setAfter(runner);
    }

    public static void setAllBefore(Runner runner) {
        INSTANCE.queueExecutor.setAfter(runner);
    }

    private static void workAfter() {
        Integer l = INSTANCE.contextManager.getContextEntity(Integer.class, "log.level");
        if (l != null) logger.setLogLevel(l.intValue());
        String format = INSTANCE.contextManager.getContextEntity(String.class, "out.format");
        if (format != null) logger.setFormat(format);
    }

    private static Set<String> fileSet = new CopyOnWriteArraySet<>();

    private static void loadConf() {
        for (String path : fileSet) {
            INSTANCE.configFileManager.load(path);
        }
    }

    private static void reloadConf() {
        for (String path : fileSet) {
            INSTANCE.configFileManager.load(path);
        }
    }

    public static Set<String> addConfFile(File file) {
        fileSet.add(file.getAbsolutePath());
        return fileSet;
    }

    public static Set<String> addConfFile(String file) {
        fileSet.add(file);
        return fileSet;
    }

    private static void work(Class<?> main) {
        try {
            Object main_ = INSTANCE.instanceCrater.create(main, INSTANCE.contextManager);
            INSTANCE.contextManager.append(main_);
            INSTANCE.classManager.add(main);
            for (Class<?> aClass : INSTANCE.packageScanner.scan(scanPath)) {
                INSTANCE.classManager.add(aClass);
            }
            logger.Log("start sptool success", 1);
        } catch (Exception e) {
            logger.Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);
        }
    }
}