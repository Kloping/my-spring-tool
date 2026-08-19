package io.github.kloping.spt.impls;

import io.github.kloping.spt.Setting;
import io.github.kloping.spt.util.CronScheduler;
import io.github.kloping.spt.annotations.Controller;
import io.github.kloping.spt.annotations.CronSchedule;
import io.github.kloping.spt.annotations.Schedule;
import io.github.kloping.spt.annotations.TimeEve;
import io.github.kloping.spt.interfaces.AutomaticWiringParams;
import lombok.extern.slf4j.Slf4j;
import io.github.kloping.spt.interfaces.component.ClassManager;
import io.github.kloping.spt.interfaces.component.ContextManager;
import io.github.kloping.spt.interfaces.component.TimeMethodManager;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static io.github.kloping.spt.PartUtils.getExceptionLine;
import static io.github.kloping.spt.PartUtils.getTimeFromNowTo;

/**
 * @author HRS-Computer
 */
@Slf4j
public class TimeMethodManagerImpl implements TimeMethodManager {

    private AutomaticWiringParams automaticWiringParams;
    private ContextManager contextManager;

    public TimeMethodManagerImpl(Setting setting, ClassManager classManager, AutomaticWiringParams wiringParams) {
        this.automaticWiringParams = wiringParams;
        this.contextManager = setting.getContextManager();
        classManager.registeredAnnotation(Controller.class, this);
        setting.getSTARTED_RUNNABLE().add(() -> {
            startTimer();
        });
    }

    private ExecutorService threads = Executors.newFixedThreadPool(5);

    private void startTimer() {
        Thread thread = new Thread(() -> {
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Map.Entry<Long, Method> en = getNextTimeMethodDelay();
                    if (en == null) {
                        log.debug("计时任务结束...");
                        return;
                    }
                    long t1 = en.getKey();
                    Thread.sleep(Math.max(1L, t1));
                    Method method = en.getValue();
                    threads.execute(() -> {
                        try {
                            Class cla = method.getDeclaringClass();
                            Object o = contextManager.getContextEntity(cla);
                            Object[] objects = automaticWiringParams.wiring(method, contextManager);
                            if (o != null && objects != null) method.invoke(o, objects);
                        } catch (Exception e) {
                            log.error("Scheduled method execution failed", e);
                        }
                    });
                } catch (Exception e) {
                    if (e instanceof InterruptedException) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                    log.error("timeEve Exception\n{}", getExceptionLine(e), e);
                }
            }
        }, "spt-schedule");
        thread.setDaemon(true);
        thread.start();
    }

    private Timer timer = new Timer("spt-time-events", true);

    private Map<Class<?>, List<Map.Entry<String, Method>>> timeMethods = new ConcurrentHashMap<>();

    private Map.Entry<Long, Method> getNextTimeMethodDelay() {
        Map.Entry<Long, Method> entry = null;
        for (Class cla : timeMethods.keySet()) {
            for (Map.Entry<String, Method> e : timeMethods.get(cla)) {
                if (e.getValue().isAnnotationPresent(Schedule.class)) {
                    String[] sss = e.getKey().split(":");
                    int n1 = Integer.parseInt(sss[0]);
                    int n2 = Integer.parseInt(sss[1]);
                    int n3 = Integer.parseInt(sss[2]);
                    long t = getTimeFromNowTo(n1, n2, n3);
                    t = t > 0 ? t : t + (1000 * 60 * 60 * 24);
                    if (entry == null) {
                        entry = new AbstractMap.SimpleEntry<>(t, e.getValue());
                    } else {
                        if (t > entry.getKey()) continue;
                        else entry = new AbstractMap.SimpleEntry<>(t, e.getValue());
                    }
                }
            }
        }
        return entry;
    }

    @Override
    public void manager(Method method, ContextManager contextManager) throws IllegalAccessException, InvocationTargetException {
        this.contextManager = contextManager;
        if (method.isAnnotationPresent(TimeEve.class)) {
            method.setAccessible(true);
            TimeEve timeEve = method.getDeclaredAnnotation(TimeEve.class);
            long t = timeEve.value();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    try {
                        Class cla = method.getDeclaringClass();
                        Object o = contextManager.getContextEntity(cla);
                        Object[] objects = automaticWiringParams.wiring(method, contextManager);
                        method.invoke(o, objects);
                    } catch (Exception e) {
                        log.error("timeEve Exception\n{}", getExceptionLine(e), e);
                    }
                }
            }, t, t);
            log.debug("new timeEve {} from {}", method.getName(), method.getDeclaringClass().getSimpleName());
        } else if (method.isAnnotationPresent(Schedule.class)) {
            Class cla = method.getDeclaringClass();
            List<Map.Entry<String, Method>> list = timeMethods.get(cla);
            if (list == null) list = new ArrayList<>();
            Schedule sch = method.getAnnotation(Schedule.class);
            String[] ss = sch.value().split(",");
            for (String s : ss) {
                list.add(new AbstractMap.SimpleEntry<>(s, method));
            }
            timeMethods.put(cla, list);
            log.debug("new Schedule {} from {}", method.getName(), method.getDeclaringClass().getSimpleName());
        } else if (method.isAnnotationPresent(CronSchedule.class)) {
            Object o = contextManager.getContextEntity(method.getDeclaringClass());
            CronSchedule schedule = method.getAnnotation(CronSchedule.class);
            String cron = schedule.value();
            if (!cron.isEmpty()) {
                method.setAccessible(true);
                CronScheduler.schedule(cron, () -> {
                    try {
                        Object[] objects = automaticWiringParams.wiring(method, contextManager);
                        method.invoke(o, objects);
                    } catch (IllegalAccessException e) {
                        log.error("Cron scheduled method access failed", e);
                    } catch (InvocationTargetException e) {
                        log.error("Cron scheduled method invocation failed", e);
                    }
                });
            }
        }
    }

    @Override
    public void manager(Class clas, ContextManager contextManager) throws IllegalAccessException, InvocationTargetException {
        for (Method method : clas.getDeclaredMethods()) {
            this.manager(method, contextManager);
        }
    }

    public void shutdown() {
        timer.cancel();
        threads.shutdownNow();
    }
}
