package io.github.kloping.spt.util;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ThreadFactory;
import java.time.LocalDateTime;

public final class CronScheduler {
    private static final ThreadFactory THREAD_FACTORY = runnable -> {
        Thread thread = new Thread(runnable, "spt-cron");
        thread.setDaemon(true);
        return thread;
    };
    private static final ScheduledExecutorService EXECUTOR = Executors.newScheduledThreadPool(1, THREAD_FACTORY);
    private CronScheduler() { }

    public static void schedule(String expression, Runnable task) {
        if (expression == null || task == null) throw new IllegalArgumentException("Cron expression and task are required");
        String[] fields = expression.trim().split("\\s+");
        if (fields.length != 5 && fields.length != 6) {
            throw new IllegalArgumentException("Cron expression must contain 5 or 6 fields: " + expression);
        }
        EXECUTOR.scheduleAtFixedRate(() -> {
            LocalDateTime now = LocalDateTime.now().withNano(0);
            if (matches(fields, now)) task.run();
        }, 0, 1, TimeUnit.SECONDS);
    }

    public static void shutdown() {
        EXECUTOR.shutdownNow();
    }

    private static boolean matches(String[] fields, LocalDateTime now) {
        if (fields.length == 6 && !matches(fields[0], now.getSecond())) return false;
        int offset = fields.length == 6 ? 1 : 0;
        if (!matches(fields[offset], now.getMinute()) || !matches(fields[offset + 1], now.getHour())
                || !matches(fields[offset + 3], now.getMonthValue())) return false;
        boolean dayOfMonth = matches(fields[offset + 2], now.getDayOfMonth());
        boolean dayOfWeek = matches(fields[offset + 4], now.getDayOfWeek().getValue() % 7);
        boolean domWildcard = fields[offset + 2].equals("*");
        boolean dowWildcard = fields[offset + 4].equals("*");
        return matches(fields[offset], now.getMinute())
                && ((domWildcard || dowWildcard) ? dayOfMonth && dayOfWeek : dayOfMonth || dayOfWeek);
    }

    private static boolean matches(String expression, int value) {
        for (String part : expression.split(",")) {
            String item = part.trim();
            if (item.equals("*")) return true;
            if (item.startsWith("*/") && value % Integer.parseInt(item.substring(2)) == 0) return true;
            if (item.contains("-")) {
                String[] range = item.split("-", 2);
                if (value >= Integer.parseInt(range[0]) && value <= Integer.parseInt(range[1])) return true;
            } else if (Integer.parseInt(item) == value) return true;
        }
        return false;
    }
}
