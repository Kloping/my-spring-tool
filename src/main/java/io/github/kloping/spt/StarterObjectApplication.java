package io.github.kloping.spt;

import io.github.kloping.spt.util.Judge;
import io.github.kloping.spt.annotations.ComponentScan;
import io.github.kloping.spt.entity.interfaces.Runner;
import io.github.kloping.spt.exceptions.NoRunException;
import io.github.kloping.spt.impls.ExtensionImpl0;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.CountDownLatch;

import static io.github.kloping.spt.PartUtils.*;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * @author github-kloping
 */
@Slf4j
public final class StarterObjectApplication {
    private static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();
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
        check(SCAN_LOADER, v);
        paths.add(v);
        String[] ps = scan.path();
        if (ps != null) {
            for (String s : scan.path()) {
                try {
                    s = filter(s, cla);
                    check(SCAN_LOADER, s);
                    paths.add(s);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
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
                log.error("Started runnable execution failed", e);
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
        if (objects == null || objects.length < 2 || objects[0] == null || objects[1] == null) {
            throw new IllegalArgumentException("executeMethod requires a key and action");
        }
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
            if (startClass == null) throw new IllegalStateException("Unable to create application class: " + main.getName());
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
            log.info("version 0.7.2-L1 sptool start success.");
        } catch (Throwable e) {
            log.error(getExceptionLine(e), e);
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
                        log.error("Post-scan runnable execution failed", e);
                    }
                    cdl.countDown();
                }
            });
        }
        try {
            cdl.await();
        } catch (InterruptedException e) {
            log.error("Post-scan interrupted", e);
        }
    }

    private void preScan() {
        ExtensionImpl0.INSTANCE = new ExtensionImpl0(getInstance());
        for (Runnable runnable : PRE_SCAN_RUNNABLE) {
            try {
                runnable.run();
            } catch (Exception e) {
                log.error(getExceptionLine(e), e);
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
