package io.github.kloping.MySpringTool;

import io.github.kloping.MySpringTool.annotations.CommentScan;
import io.github.kloping.MySpringTool.entity.interfaces.Runner;
import io.github.kloping.MySpringTool.exceptions.NoRunException;
import io.github.kloping.MySpringTool.h1.impl.ExtensionImpl0;
import io.github.kloping.MySpringTool.h1.impl.LoggerImpl;
import io.github.kloping.MySpringTool.interfaces.Logger;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;

import static io.github.kloping.MySpringTool.PartUtils.*;
import static io.github.kloping.common.Public.EXECUTOR_SERVICE;

/**
 * @author github-kloping
 */
public final class StarterApplication {
    public static final class Setting {
        public static io.github.kloping.MySpringTool.Setting INSTANCE;
    }

    public static Logger logger = new LoggerImpl();
    public static ClassLoader SCAN_LOADER = StarterApplication.class.getClassLoader();
    private static int poolSize = 20;
    private static long waitTime = 12 * 1000L;
    private static Class<?> mainKey = Long.class;
    private static String scanPath;
    private static boolean inited = false;
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
            Object startClass = getInstance().getInstanceCrater().create(main, getInstance().contextManager);
            getInstance().getContextManager().append(startClass);
            getInstance().getClassManager().add(main);
            preScan();
            for (Class<?> aClass : getInstance().getPackageScanner().scan(main, SCAN_LOADER, scanPath)) {
                getInstance().getClassManager().add(aClass);
            }
            postScan();
            logger.info("start sptool success");
        } catch (Throwable e) {
            logger.error("There is an exception=>" + e + " at " + getExceptionLine(e));
            e.printStackTrace();
        }
    }

    private static void postScan() {
        CountDownLatch cdl = new CountDownLatch(POST_SCAN_RUNNABLE.size());
        for (Runnable runnable : POST_SCAN_RUNNABLE) {
            EXECUTOR_SERVICE.submit(new Runnable() {
                @Override
                public void run() {
                    try {
                        runnable.run();
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                    cdl.countDown();
                }
            });
        }
        try {
            cdl.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static void preScan() {
        ExtensionImpl0.INSTANCE = new ExtensionImpl0(getInstance());
        for (Runnable runnable : PRE_SCAN_RUNNABLE) {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.error("There is an exception=>" + e + " at " + getExceptionLine(e));
            }
        }
    }

    private static io.github.kloping.MySpringTool.Setting getInstance() {
        synchronized (StarterApplication.class) {
            if (!inited) {
                Setting.INSTANCE = new io.github.kloping.MySpringTool.Setting() {

                    @Override
                    public List<Runnable> getSTARTED_RUNNABLE() {
                        return STARTED_RUNNABLE;
                    }

                    @Override
                    public List<Runnable> getPRE_SCAN_RUNNABLE() {
                        return PRE_SCAN_RUNNABLE;
                    }

                    @Override
                    public List<Runnable> getPOST_SCAN_RUNNABLE() {
                        return POST_SCAN_RUNNABLE;
                    }
                };
                Setting.INSTANCE.defaultInit(mainKey, poolSize, waitTime);
                inited = true;
            }
        }
        return Setting.INSTANCE;
    }

}