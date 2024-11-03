package io.github.kloping.spt;

import io.github.kloping.judge.Judge;
import io.github.kloping.spt.annotations.ComponentScan;
import io.github.kloping.spt.entity.interfaces.Runner;
import io.github.kloping.spt.exceptions.NoRunException;
import io.github.kloping.spt.impls.ExtensionImpl0;
import io.github.kloping.spt.impls.LoggerImpl;
import io.github.kloping.spt.interfaces.Logger;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;

import static io.github.kloping.common.Public.EXECUTOR_SERVICE;
import static io.github.kloping.spt.PartUtils.*;


/**
 * @author github-kloping
 */
public final class StarterObjectApplication {
    public Logger logger = new LoggerImpl();
    public Setting INSTANCE = null;

    private final ClassLoader SCAN_LOADER;

    public StarterObjectApplication(ClassLoader SCAN_LOADER) {
        this.SCAN_LOADER = SCAN_LOADER;
    }

    public StarterObjectApplication(Class cla) {
        this(cla.getClassLoader());
    }

    private int poolSize = 20;
    private long waitTime = 12 * 1000L;
    private String[] scanPaths;
    private boolean inited = false;
    private Class<?> mainKey = Long.class;
    /**
     * started runnable
     */
    public final List<Runnable> STARTED_RUNNABLE = new LinkedList<>();
    /**
     * on scan before
     */
    public final List<Runnable> PRE_SCAN_RUNNABLE = new LinkedList<>();
    /**
     * on scan after
     */
    public final List<Runnable> POST_SCAN_RUNNABLE = new LinkedList<>();

    private Set<String> fileSet = new CopyOnWriteArraySet<>();

    public void setPoolSize(int poolSize) {
        this.poolSize = poolSize;
    }

    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }

    /**
     * start SpringTool
     *
     * @param cla
     */
    public StarterObjectApplication run0(Class<?> cla) {
        if (cla.isAnnotationPresent(ComponentScan.class)) {
            ComponentScan scan = cla.getAnnotation(ComponentScan.class);
            scanPaths = loadPaths(scan, cla);
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
        return this;
    }

    private String[] loadPaths(ComponentScan scan, Class<?> cla) {
        List<String> paths = new LinkedList<>();
        String v = filter(scan.value(), cla);
        check(v);
        paths.add(v);
        String[] ps = scan.path();
        if (ps != null) {
            for (String s : scan.path()) {
                try {
                    s = filter(s, cla);
                    check(s);
                    paths.add(s);
                } catch (Exception e) {
                    System.err.println(e.getMessage());
                    continue;
                }
            }
        }
        return paths.toArray(new String[0]);
    }

    public static StarterObjectApplication run(Class<?> cla) {
        return new StarterObjectApplication(cla.getClassLoader()).run0(cla);
    }

    private void startAfter() {
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
    public void setMainKey(Class<?> cla) {
        mainKey = cla;
    }

    /**
     * set Access Types
     *
     * @param classes
     */
    public void setAccessTypes(Class<?>... classes) {
        getInstance().argsManager.setArgsType(classes);
    }

    /**
     * matcher and run
     *
     * @param objects
     * @return
     */
    public synchronized int executeMethod(Object... objects) {
        return getInstance().queueExecutor.queueExecute(objects[0], objects);
    }

    /**
     * on matched and run ed
     *
     * @param runner
     */
    public void setAllAfter(Runner runner) {
        getInstance().queueExecutor.setAfter(runner);
    }

    /**
     * on run before
     *
     * @param runner
     */
    public void setAllBefore(Runner runner) {
        getInstance().queueExecutor.setBefore(runner);
    }

    private void workAfter() {
        Integer l = getInstance().contextManager.getContextEntity(Integer.class, "log.level");
        if (l != null) logger.setLogLevel(l.intValue());
        String format = getInstance().contextManager.getContextEntity(String.class, "out.format");
        if (format != null) logger.setFormat(new SimpleDateFormat(format));
        INSTANCE.getContextManager().append(logger, logger.getClass().getSimpleName());
    }

    private void loadConf() {
        for (String path : fileSet) {
            getInstance().configFileManager.load(path);
        }
    }

    private void reloadConf() {
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
    public Set<String> addConfFile(File file) {
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
    public Set<String> addConfFile(String file) {
        fileSet.add(file);
        return fileSet;
    }

    private void work(Class<?> main) {
        try {
            Object startClass = getInstance().getInstanceCrater().create(main, getInstance().contextManager);
            getInstance().getContextManager().append(logger, logger.getClass().getSimpleName());
            getInstance().getContextManager().append(startClass);
            getInstance().getClassManager().add(main);
            preScan();
            for (String scanPath : scanPaths) {
                if (Judge.isEmpty(scanPath)) continue;
                for (Class<?> aClass : getInstance().getPackageScanner().scan(main, SCAN_LOADER, scanPath)) {
                    getInstance().getClassManager().add(aClass);
                }
            }
            postScan();
            logger.info("version 0.6.3-R sptool start success");
        } catch (Throwable e) {
            logger.error(getExceptionLine(e));
            e.printStackTrace();
        }
    }

    private void postScan() {
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

    private void preScan() {
        ExtensionImpl0.INSTANCE = new ExtensionImpl0(getInstance());
        for (Runnable runnable : PRE_SCAN_RUNNABLE) {
            try {
                runnable.run();
            } catch (Exception e) {
                logger.error(getExceptionLine(e));
            }
        }
    }

    private Setting getInstance() {
        synchronized (this) {
            if (!inited) {
                INSTANCE = new Setting() {

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
                INSTANCE.defaultInit(mainKey, poolSize, waitTime);
                inited = true;
            }
        }
        return INSTANCE;
    }

    public boolean isInited() {
        return inited;
    }

    public void setInited(boolean inited) {
        this.inited = inited;
    }
}