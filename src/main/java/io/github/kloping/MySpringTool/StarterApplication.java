package io.github.kloping.MySpringTool;

import io.github.kloping.MySpringTool.annotations.CommentScan;
import io.github.kloping.MySpringTool.entity.interfaces.Runner;
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
import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import static io.github.kloping.MySpringTool.partUtils.*;

/**
 * @author github-kloping
 */
public final class StarterApplication {
    public static Logger logger;

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
//            if (dataBaseManager == null)
//                dataBaseManager = new DataBaseManagerImpl(classManager);
            inited = true;
            Field[] fields = Setting.class.getDeclaredFields();
            for (Field field : fields) {
                Object o = null;
                try {
                    o = field.get(this);
                } catch (Exception e) {
                    continue;
                }
                if (o != null)
                    contextManager.append(o);
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

    public static void setPoolSize(int poolSize) {
        StarterApplication.poolSize = poolSize;
    }

    public static void setWaitTime(long waitTime) {
        StarterApplication.waitTime = waitTime;
    }

    /**
     * 启动 SpringTool
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
                throw new NoRunException("此类上必须存在 CommentScan 注解 (class must has @interface CommentScan )");
            } finally {
                System.exit(0);
            }
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

    /**
     * 设置主键参数
     * 约束不可同一主键 多个运行
     *
     * @param cla
     */
    public static void setMainKey(Class<?> cla) {
        mainKey = cla;
    }

    /**
     * 设置接收参数
     *
     * @param classes
     */
    public static void setAccessTypes(Class<?>... classes) {
        getInstance().argsManager.setArgsType(classes);
    }

    /**
     * 开始匹配运行
     *
     * @param objects
     * @return
     */
    public static synchronized int ExecuteMethod(Object... objects) {
        return getInstance().queueExecutor.queueExecute(objects[0], objects);
    }

    /**
     * 设置所有运行之后
     *
     * @param runner
     */
    public static void setAllAfter(Runner runner) {
        getInstance().queueExecutor.setAfter(runner);
    }

    /**
     * 设置所有运行之前
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
     * 添加 一个配置文件
     * 并返回 当前所有配置文件
     *
     * @param file
     * @return
     */
    public static Set<String> addConfFile(File file) {
        fileSet.add(file.getAbsolutePath());
        return fileSet;
    }

    /**
     * 添加 一个配置文件
     * 并返回 当前所有配置文件
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
            for (Class<?> aClass : getInstance().packageScanner.scan(scanPath)) {
                getInstance().classManager.add(aClass);
            }
            logger.Log("start sptool success", 1);
        } catch (Exception e) {
            logger.Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);
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