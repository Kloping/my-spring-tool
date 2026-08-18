package io.github.kloping.spt.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class CronScheduler {
    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1);
    private CronScheduler() { }

    public static void schedule(String expression, Runnable task) {
        long interval = 60L;
        String[] fields = expression.trim().split("\\s+");
        if (fields.length > 0 && fields[0].startsWith("*/")) {
            try { interval = Math.max(1L, Long.parseLong(fields[0].substring(2))); } catch (NumberFormatException ignored) { }
        }
        EXECUTOR.scheduleAtFixedRate(task, interval, interval, TimeUnit.SECONDS);
    }
}
