package com.hrs.MySpringTool;


import com.hrs.MySpringTool.annotations.*;
import com.hrs.MySpringTool.exceptions.NoRunException;

import java.io.*;
import java.lang.reflect.*;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLClassLoader;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static cn.kloping.object.ObjectUtils.baseToPack;

public final class Starter {
    private static String scanPath = "";
    private static Object main;
    private static Class<?>[] accPars = null;
    private static Class<?> _key = null;
    private static Integer Log_Level = 0;
    private static Long waitTime = 15L;
    private static boolean onlyOne = false;

    public static void set_key(Class<?> _key) {
        Starter._key = _key;
    }

    public static final void setAccPars(Class<?>... classes) {
        accPars = classes;
        Log("设置 接收参数 类型 完成 (Set Access Parameters Successful)=>" + Arrays.toString(classes), 0);
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
            Log("开始扫描主类 Bean(Start Scan Main Class Bean)", 1);
            loadConf();
            startScanMainBean(cla, 1);
            Log("扫描主类 Bean 完成(Scan Main Class Bean Complete)", 1);
            Log("开始 扫描 所有 包(Start Scan All Class On Package)", 1);
            startScan(cla);
            InitMaybeKey();
            Log("准备完成(All is Paper)", 1);
            startTimer();
        } else {
            throw new NoRunException("此类上必须存在 CommentScan 注解 (class must has @interface CommentScan )");
        }
    }

    private static void startTimer() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Map.Entry<Long, Method> en = getNextTimeMethodDelay();
                    if (en == null) {
                        Log("计时任务结束...", 2);
                        return;
                    }
                    long t1 = en.getKey();
                    if (t1 > 0) {
                        Thread.sleep(t1);
                        Method method = en.getValue();
                        threads.execute(() -> {
                            try {
                                method.invoke(null);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                        run();
                    }
                } catch (Exception e) {
                    Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);
                }
            }
        }).start();
    }

    private static Map.Entry<Long, Method> getNextTimeMethodDelay() {
        Map.Entry<Long, Method> entry = null;
        for (Class cla : timeMethods.keySet()) {
            for (Map.Entry<String, Method> e : timeMethods.get(cla)) {
                if (e.getValue().isAnnotationPresent(Schedule.class)) {
                    String[] sss = e.getKey().split(":");
                    int n1 = Integer.parseInt(sss[0]);
                    int n2 = Integer.parseInt(sss[1]);
                    int n3 = Integer.parseInt(sss[2]);
                    long t = Starter.Utils.getTimeFromNowTo(n1, n2, n3);
                    t = t > 0 ? t : t + (1000 * 60 * 60 * 24);
                    if (entry == null) {
                        entry = getEntry(t, e.getValue());
                    } else {
                        if (t > entry.getKey())
                            continue;
                        else entry = getEntry(t, e.getValue());
                    }
                }
            }
        }
        return entry;
    }

    private static void loadConf() {
        Class<?>[] classes = new Class[]{String.class, Long.class, Integer.class, Boolean.class, long.class, int.class, boolean.class};
        for (String k : configurationMap.keySet()) {
            try {
                for (Class<?> cla : classes) {
                    try {
                        Object v = configurationMap.get(k).trim();
                        if (cla != String.class) {
                            Method method = baseToPack(cla).getMethod("valueOf", String.class);
                            v = method.invoke(null, v.toString());
                        }
                        if (cla == Boolean.class) {
                            if (v.toString().trim().equals("false") || v.toString().trim().equals("true")) {
                                v = Boolean.valueOf(v.toString());
                            } else {
                                continue;
                            }
                        }
                        Map<String, Object> map = ObjMap.get(cla);
                        if (map == null) map = new ConcurrentHashMap<>();
                        map.put(k, v);
                        ObjMap.put(cla, map);
                    } catch (Exception e) {
                        continue;
                    }
                }
            } catch (Exception e) {
                Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);
                continue;
            }
        }
    }

    private static String filter(String path, Class cla) {
        if (path.equals(".") || path.equals("/") || path.equals("./") || path.trim().isEmpty()) {
            path = cla.getName().substring(0, cla.getName().indexOf("."));
        }
        return path;
    }

    private static void InitMaybeKey() {
        for (String key : actions.keySet()) {
            Character c = key.charAt(0);
            if (c != '\\')
                maybeKeys.add(c);
            else
                maybeKeys.add(key.charAt(1));
        }
    }

    public static void setAllAfter(AllAfterOrBefore allAfter) {
        Starter.allAfter = allAfter;
    }

    public static void setAllBefore(AllAfterOrBefore allBefore) {
        Starter.allBefore = allBefore;
    }

    public static Long getWaitTime() {
        return waitTime;
    }

    public static final Map<String, String> configurationMap = new LinkedHashMap<>();

    /**
     * 加载配置文件
     * 格式为
     * k=v
     * V类型可为 String Long Integer
     * 使用AutoStand 自动填充
     *
     * @param file
     */
    public synchronized static void loadConfigurationFile(File file) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line = null;
            while ((line = br.readLine()) != null) {
                try {
                    String[] ss = line.split("=");
                    configurationMap.put(ss[0].trim(), ss[1].trim());
                } catch (Exception e) {
                    continue;
                }
            }
            br.close();
        } catch (Exception e) {
            Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);
        }
    }

    /**
     * 重新加载配置文件
     *
     * @param file
     */
    public synchronized static void reLoadConfigurationFile(File file) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));
            String line = null;
            while ((line = br.readLine()) != null) {
                try {
                    String[] ss = line.split("=");
                    configurationMap.put(ss[0].trim(), ss[1].trim());
                } catch (Exception e) {
                    continue;
                }
            }
            br.close();
            loadConf();
        } catch (Exception e) {
            Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);
        }
    }

    /**
     * 加载配置文件
     * 格式为
     * k=v
     * V类型可为 String Long Integer
     * 使用AutoStand 自动填充
     *
     * @param filePath
     */
    public synchronized static void loadConfigurationFile(String filePath) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            String line = null;
            while ((line = br.readLine()) != null) {
                try {
                    String[] ss = line.split("=");
                    configurationMap.put(ss[0].trim(), ss[1].trim());
                } catch (Exception e) {
                    continue;
                }
            }
            br.close();
        } catch (Exception e) {
            Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);
        }
    }

    /**
     * 重新加载配置文件
     *
     * @param filePath
     */
    public synchronized static void reLoadConfigurationFile(String filePath) {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
            String line = null;
            while ((line = br.readLine()) != null) {
                try {
                    String[] ss = line.split("=");
                    configurationMap.put(ss[0].trim(), ss[1].trim());
                } catch (Exception e) {
                    continue;
                }
            }
            br.close();
            loadConf();
        } catch (Exception e) {
            Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);
        }
    }

    public static void setWaitTime(Long waitTime) {
        Starter.waitTime = waitTime;
    }

    public static void setLog_Level(Integer log_Level) {
        Log_Level = log_Level;
    }

    private static boolean tryAll = false;

    /**
     * 开始尝试匹配运行
     *
     * @param objects
     * @return
     */
    public static final int ExecuteMethod(Object... objects) {
        final Object[] objs = baseToPack(objects);
        if (((String) objs[1]).isEmpty()) {
            Log("不可能的匹配(impossible match)=>" + Arrays.toString(objects), 2);
            return -1;
        }
        String js = (objs[1].toString());
        char c = js.charAt(0);
        if (!tryAll && !maybeKeys.contains(c)) {
            Log("不可能的匹配(impossible match)=>" + Arrays.toString(objects), 2);
            return -1;
        }
        if (accept(ObjectsToClasses(objs))) {
            Log("开始匹配(Start match)=>" + Arrays.toString(objects), 0);
            if (!runList.add(objs[0])) {
                runQueue.offer(objs);
                Log("稍后执行(Execute later)-->" + Arrays.toString(objs), 2);
                return runList.size();
            }
            try {
                return runList.size();
            } finally {
                threads.execute(new A(objs) {
                    @Override
                    public void run() {
                        long l1 = System.currentTimeMillis();
                        Future future = threads.submit(new Runnable() {
                            @Override
                            public void run() {
                                Run(true, objs);
                            }
                        });
                        try {
                            future.get(waitTime, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Log("运行时错误(Running Has Error)=>" + e, -1);
                            future.cancel(true);
                        } catch (TimeoutException e) {
                            Log("运行超时(Run Time Out)=>" + e, -1);
                            future.cancel(true);
                        } catch (Exception e) {
                            Log("其他错误(Other Error)=>" + e + "\n", -1);
                            e.printStackTrace();
                            future.cancel(true);
                        }
                        Log("活动处理结束(Run Complete)=>" + Arrays.toString(objs), 0);
                        runList.remove(objs[0]);
                        Log("耗时(Use Timed)=>" + (System.currentTimeMillis() - l1) + "ms", 1);
                        Log("============================================", 1);
                        if (!runQueue.isEmpty()) {
                            Object[] objects = runQueue.poll();
                            Log("开始回调(start calling)=>" + Arrays.toString(objects), 1);
                            ExecuteMethod(objects);
                        }
                        System.gc();
                    }
                });
            }
        } else {
            Log("不接受这组参数(Not Access This Parameters)=>" + Arrays.toString(objs), -1);
            return -1;
        }
    }

    /**
     * 是否尝试所有的匹配
     * (不会报 不可能的匹配 )
     * 适合于 存在  正则匹配开头的 Action
     *
     * @param tryAll
     */
    public static void setTryAll(boolean tryAll) {
        Starter.tryAll = tryAll;
    }

    private static Class<?>[] ObjectsToClasses(Object... objs) {
        Class<?>[] classes = new Class[objs.length];
        for (int i = 0; i < objs.length; i++) {
            classes[i] = objs[i].getClass();
        }
        return classes;
    }

    /**
     * 如果 不需要扫描 或者 不支持扫描 则设置为 false 并把类元素加入 classes 集合
     */
    public static final boolean NeedScan = true;

    /**
     * 阅读 NessScan 注解
     */
    public static final Set<Class<?>> AllClass = new CopyOnWriteArraySet<>();
    private static final Set<Character> maybeKeys = new CopyOnWriteArraySet<>();

    private static abstract class A implements Runnable {
        protected Object[] objects;

        public A(Object[] objects) {
            this.objects = objects;
        }

    }

    public static abstract class AllAfterOrBefore {
        private State state = State.Before;

        public AllAfterOrBefore(State state) {
            this.state = state;
        }

        public abstract void run(Object ret, Object[] o_objs) throws NoRunException;

        public enum State {
            Before(),
            After()
        }
    }

    //============================

    /**
     * 0 Normal Withe
     * 1 Info Green
     * 2 Debug Yellow
     * -1 Err Red
     *
     * @param mess
     * @param level
     */
    private static synchronized void Log(String mess, int level) {
        if (level != -1 && level < Log_Level) return;
        String info = "[" + df.format(new Date()) + "]" + "=>" + mess;
        switch (level) {
            case 0:
                info = "[Normal]" + info;
                break;
            case 1:
                info = "[Info]  " + info;
                break;
            case 2:
                info = "[Debug] " + info;
                break;
            case -1:
                info = "[Error] " + info;
                break;
        }
        info = "[com.hrs.Spring]" + info;
        if (level == 0) {
            System.out.println(info);
        } else if (level == 1) {
            System.out.format("\033[32m" + info + "\033[m\n");
        } else if (level == 2) {
            System.out.format("\033[33m" + info + "\033[m\n");
        } else if (level == -1) {
            System.err.println(info);
        }
    }

    private static final SimpleDateFormat df = new SimpleDateFormat("MM/dd-HH:mm:ss:SSS");

    private static final void appendToObjMap(String id, Object obj) {
        Map map = ObjMap.get(obj.getClass());
        if (map == null) map = new ConcurrentHashMap();
        map.put(id, obj);
        ObjMap.put(obj.getClass(), map);
        PutAllInterface(id, obj.getClass(), obj);
    }

    private static final void appendToObjMap(String id, Object obj, Class<?> cla) {
        Map map = ObjMap.get(cla);
        if (map == null) map = new ConcurrentHashMap();
        map.put(id, obj);
        ObjMap.put(cla, map);
        PutAllInterface(id, cla, obj);
    }

    private static final void PutAllInterface(String id, Class<?> cla, Object obj) {
        Class<?>[] clas = cla.getInterfaces();
        for (Class<?> cla1 : clas) {
            appendToObjMap(id, obj, cla1);
        }
    }

    /**
     * 线程池
     */
    private static final ExecutorService threads = Executors.newFixedThreadPool(40);
    /**
     * 运行队列
     */
    private static final Set<Object> runList = new HashSet<>();
    private static final Queue<Object[]> runQueue = new LinkedList<>();
    /**
     * 活动储存
     */
    private static final Map<String, Map.Entry<Object, Method>> actions = new ConcurrentHashMap();

    /**
     * 活动Before储存
     */
    private static final Map<Class<?>, Method> beforeS = new ConcurrentHashMap<>();

    /**
     * 活动after储存
     */
    private static final Map<Class<?>, Method> afterS = new ConcurrentHashMap<>();

    /**
     * 实例 Object map
     */
    private static final Map<Class<?>, Map<String, Object>> ObjMap = new ConcurrentHashMap<>();

    /**
     * 所有类字节码
     */
    private static final Set<Class<?>> classes = new CopyOnWriteArraySet<>();

    private static AllAfterOrBefore allAfter = null;

    private static AllAfterOrBefore allBefore = null;

    private static final Map<String, String> histRunedRV = new ConcurrentHashMap<>();

    /**
     * 匹配到一个就停止匹配
     *
     * @param onlyOne
     */
    public static void setOnlyOne(boolean onlyOne) {
        Starter.onlyOne = onlyOne;
    }

    /**
     * 运行
     *
     * @param objs
     */
    private static boolean Run(boolean run, Object... objs) {
        boolean k = false;
        String res = objs[1].toString();
        Result result = null;
        if (histRunedRV.containsKey(res)) {
            String v = histRunedRV.get(res);
            if ((result = Result.create(res, v)).isMatch()) {
                histRunedRV.put(res, v);
                if (!run) return true;
                Log("匹配并运行(mather and run)=>" + Arrays.toString(objs), 1);
                result.setObjs(objs);
                RunMethod(actions.get(v), result);
                k = true;
                if (onlyOne) return true;
            }
            return k;
        }
        for (String v : actions.keySet()) {
            if (!tryAll && !maybe(res, v)) continue;
            if ((result = Result.create(res, v)).isMatch()) {
                if (result.state >= 0)
                    histRunedRV.put(res, v);
                if (!run) return true;
                Log("匹配并运行(mather and run)=>" + Arrays.toString(objs), 1);
                result.setObjs(objs);
                RunMethod(actions.get(v), result);
                k = true;
                if (onlyOne) return true;
            }
        }
        if (!k)
            Log("无匹配 (no mather)=>" + Arrays.toString(objs), 2);
        return k;
    }

    public static boolean matcher(String str) {
        if (!maybeKeys.contains(str.charAt(0))) {
            return false;
        }
        for (String v : actions.keySet()) {
            String res = str;
            if (!maybe(res, v)) continue;
            Result result = null;
            if ((result = Result.create(res, v)).isMatch()) {
                return true;
            }
        }
        return false;
    }

    private static void RunMethod(Map.Entry<Object, Method> objectMethodEntry, Result result) {
        try {
            Class<?> cla = objectMethodEntry.getKey().getClass();
            Method method = objectMethodEntry.getValue();
            Object obj = objectMethodEntry.getKey();
            Parameter[] pars = method.getParameters();
            Object[] objs = result.getObjs();
            if (beforeS.containsKey(cla)) {
                if (allBefore != null) {
                    if (allBefore.state == AllAfterOrBefore.State.After) {
                        if (!RunBeforeOrAfter(beforeS.get(cla), objectMethodEntry.getKey(), result.getObjs(), result)) {
                            return;
                        }
                        allBefore.run(null, objs);
                    } else {
                        allBefore.run(null, objs);
                        if (!RunBeforeOrAfter(beforeS.get(cla), objectMethodEntry.getKey(), result.getObjs(), result)) {
                            return;
                        }
                    }
                } else {
                    if (!RunBeforeOrAfter(beforeS.get(cla), objectMethodEntry.getKey(), result.getObjs(), result)) {
                        return;
                    }
                }
            } else if (allBefore != null) {
                allBefore.run(null, objs);
            }
            //=======================================
            Object[] objPars = AutoObjFromPar(pars, objs);
            objPars = AutoObjOnPar(pars, objPars, result);
            method.setAccessible(true);
            Object ret = method.invoke(obj, objPars);
            //=======================================
            if (afterS.containsKey(cla)) {
                if (allAfter != null) {
                    if (allAfter.state == AllAfterOrBefore.State.After) {
                        if (!RunBeforeOrAfter(afterS.get(cla), objectMethodEntry.getKey(), objAndObjsToObjs(ret, result.getObjs()), result))
                            return;
                        allAfter.run(ret, objs);
                    } else {
                        allAfter.run(ret, objs);
                        if (!RunBeforeOrAfter(afterS.get(cla), objectMethodEntry.getKey(), objAndObjsToObjs(ret, result.getObjs()), result))
                            return;
                    }
                } else {
                    if (!RunBeforeOrAfter(afterS.get(cla), objectMethodEntry.getKey(), objAndObjsToObjs(ret, result.getObjs()), result))
                        return;
                }
            } else if (allAfter != null) {
                allAfter.run(ret, objs);
            }
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                getTargetException((InvocationTargetException) e);
                return;
            }
            Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);
        }
    }

    private static Object[] objAndObjsToObjs(Object o, Object... objects) {
        List<Object> list = new ArrayList<>();
        list.add(o);
        list.addAll(Arrays.asList(objects));
        return list.toArray(new Object[0]);
    }

    private static final String getExceptionLine(Throwable e) {
        try {
            Method method = Throwable.class.getDeclaredMethod("getOurStackTrace");
            method.setAccessible(true);
            Object[] objects = (Object[]) method.invoke(e);
            StringBuilder sb = new StringBuilder("\r\n");
            for (Object o : objects) {
                sb.append(" at ").append(o.toString()).append("\r\n\t");
            }
            return sb.toString();
        } catch (Exception e1) {
            return "??";
        }
    }

    private static boolean RunBeforeOrAfter(Method method, Object key, Object[] objs, Result result) {
        try {
            Parameter[] parameters = method.getParameters();
            Object[] objects = AutoObjFromPar(parameters, objs);
            objects = AutoObjOnPar(parameters, objects, result);
            method.invoke(key, objects);
            return true;
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                getTargetException((InvocationTargetException) e);
                return false;
            }
            Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);
            return false;
        }
    }

    private static void getTargetException(InvocationTargetException e) {
        InvocationTargetException ite = e;
        if (ite.getTargetException().getClass() == NoRunException.class) {
            NoRunException exception = (NoRunException) ite.getTargetException();
            Log("抛出 不运行异常(throw NuRunException): " + exception.getMessage(), 2);
        } else {
            Log("存在映射一个异常(Has a Invoke Exception)=>" + ite.getTargetException() + " at " + getExceptionLine(ite.getTargetException()), -1);
        }
    }

    private static Object[] AutoObjFromPar(Parameter[] parameters, Object[] objects) {
        Object[] objects1 = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            try {
                if (hasAnnotation(parameters[i])) continue;
                if (parameters[i].getType() == Object[].class) {
                    objects1[i] = objects;
                    continue;
                }
                Class<?> type = baseToPack(parameters[i].getType());
                int n;
                n = find(objects, type);
                if (n >= 0) {
                    objects1[i] = objects[n];
                }
            } catch (Exception e) {
                Log("赋值参数失败=>" + parameters[i].getType(), 2);
                continue;
            }
        }
        return objects1;
    }

    private static boolean hasAnnotation(Parameter parameter) {
        if (parameter.isAnnotationPresent(Param.class)) return true;
        if (parameter.isAnnotationPresent(AllMess.class)) return true;
        return false;
    }

    private static Object[] AutoObjOnPar(Parameter[] parameters, Object[] objects, Result result) {
        for (int i = 0; i < parameters.length; i++) {
            if (parameters[i].isAnnotationPresent(Param.class)) {
                Param param = parameters[i].getAnnotation(Param.class);
                String s1 = param.value();
                if (result != null && result.K != null && result.V != null)
                    if (result.getK().equals(s1)) {
                        try {
                            Class cla = parameters[i].getType();
                            cla = baseToPack(cla);
                            if (cla == Long.class) {
                                objects[i] = Long.parseLong(result.getV());
                            } else if (cla == Integer.class) {
                                objects[i] = Integer.parseInt(result.getV());
                            } else if (cla == Float.class) {
                                objects[i] = Float.parseFloat(result.getV());
                            } else if (cla == Double.class) {
                                objects[i] = Double.parseDouble(result.getV());
                            } else if (cla == Boolean.class) {
                                objects[i] = Boolean.parseBoolean(result.getV());
                            } else {
                                objects[i] = result.getV();
                            }
                        } catch (Exception e) {
                            objects[i] = result.getV();
                        }
                    }
            } else if (parameters[i].isAnnotationPresent(AllMess.class)) {
                AllMess param = parameters[i].getAnnotation(AllMess.class);
                objects[i] = result.getRes();
            }
            continue;
        }
        return objects;
    }

    private static final Map<Class[], Map<Class, Integer>> findHist = new ConcurrentHashMap<>();

    private static int find(Object[] obj, Class<?> cla) {
        Class[] classes = ObjectsToClasses(obj);
        if (findHist.containsKey(classes) && findHist.get(classes).containsKey(cla))
            return findHist.get(classes).get(cla);
        obj = baseToPack(obj);
        int n = -1;
        for (int i = 2; i < obj.length; i++) {
            if (superOrImpl(cla, obj[i].getClass()))
                n = i;
        }
        if (n >= 0) {
            Map map = findHist.get(classes);
            if (map == null) map = new ConcurrentHashMap();
            map.put(cla, n);
            findHist.put(classes, map);
        }
        return n;
    }

    private static final Map<Class<?>, List<Class>> father2son = new ConcurrentHashMap<>();
    private static final List<Class[]> acceptClasses = new CopyOnWriteArrayList<>();

    private static final boolean ListArrayContainsArray(Class<?>... classes) {
        for (Class[] classes1 : acceptClasses) {
            if (Arrays.equals(classes1, classes)) return true;
        }
        return false;
    }

    private static boolean accept(Class<?>... classes) {
        if (ListArrayContainsArray(classes))
            return true;
        if (classes[0] == _key) {
            if (classes[1] == String.class) {
                for (int i = 2; i < accPars.length + 2; i++) {
                    Class<?> cla1 = classes[i];
                    Class<?> cla2 = accPars[i - 2];
                    boolean k = superOrImpl(cla2, cla1);
                    if (k)
                        continue;
                    else return false;
                }
                if (!acceptClasses.contains(classes))
                    acceptClasses.add(classes);
                return true;
            }
        }
        return false;
    }

    private static boolean superOrImpl(final Class<?> father, final Class<?> son) {
        if (father == son) return true;
        try {
            if (father2son.get(father).contains(son)) {
                Log("从历史匹配知道 " + father + " 匹配与=>" + son, 0);
                return true;
            }
        } catch (Exception e) {
        }
        Class<?> son1 = son;
        while (hasSuper(son1)) {
            son1 = son1.getSuperclass();
            if (father == son1) {
                appendFather2Son(father, son);
                return true;
            }
            continue;
        }
        Class<?>[] classes1 = son.getInterfaces();
        if (isInterfaces(classes1, father)) {
            appendFather2Son(father, son);
            return true;
        }
        return false;
    }

    private static void appendFather2Son(Class<?> father, Class<?> son) {
        List<Class> list = father2son.get(father);
        if (list == null) list = new CopyOnWriteArrayList<>();
        if (!list.contains(son)) list.add(son);
        father2son.put(father, list);
    }

    private static boolean isInterfaces(Class<?>[] classes1, Class<?> cla) {
        if (classes1 == null || classes1.length == 0) return false;
        for (Class c : classes1) {
            if (c == cla) return true;
            else {
                if (isInterfaces(c.getInterfaces(), cla))
                    return true;
            }
        }
        return false;
    }

    private static boolean hasInterface(Class<?> cla) {
        if (cla.getInterfaces().length > 0)
            return true;
        return false;
    }

    private static boolean hasSuper(Class<?> cla) {
        if (cla.getSuperclass() != null)
            return true;
        return false;
    }

    private static boolean hasClass(Class<?>[] classes, Class<?> cla) {
        if (classes == null) return false;
        for (Class c : classes) {
            if (c == cla) return true;
        }
        return false;
    }

    private static Object newInstance(Class<?> cla) {
        Constructor<?>[] constructors = cla.getDeclaredConstructors();
        for (Constructor<?> constructor : constructors) {
            try {
                if (constructor.getParameterCount() == 0) {
                    constructor.setAccessible(true);
                    return constructor.newInstance();
                }
            } catch (Exception e) {
                continue;
            }
        }
        return null;
    }

    private static void startScanMainBean(Class<?> cla) {
        try {
            main = newInstance(cla);
            Method[] methods = cla.getDeclaredMethods();
            for (Method method : methods) {
                InitMethod(cla, main, method);
                if (method.isAnnotationPresent(Bean.class)) {
                    try {
                        Bean bean = method.getAnnotation(Bean.class);
                        String id = bean.value();
                        Class rec = method.getReturnType();
                        Object obj = method.invoke(main);
                        appendToObjMap(id, obj, rec);
                    } catch (Exception e) {
                        Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);
                    }
                }
            }
        } catch (Exception e) {
            Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);

        }
    }

    private static void startScanMainBean(Class<?> cla, int n) {
        try {
            main = newInstance(cla);
            Map<String, Object> map = new ConcurrentHashMap<>();
            map.put("main", main);
            ObjMap.put(cla, map);
            Method[] methods = cla.getDeclaredMethods();
            for (Method method : methods) {
                InitMethod(cla, main, method);
                if (method.isAnnotationPresent(Bean.class)) {
                    try {
                        Bean bean = method.getAnnotation(Bean.class);
                        String id = bean.value();
                        Class rec = method.getReturnType();
                        Object obj = method.invoke(main);
                        appendToObjMap(id, obj, rec);
                    } catch (Exception e) {
                        Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);
                    }
                }
            }
        } catch (Exception e) {
            Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);

        }
    }

    private static void startScan(Class<?> mianCla) {
        if (NeedScan) {
            Set<Class<?>> sets = getClassName(scanPath, true);
            classes.addAll(sets);
        } else
            classes.addAll(AllClass);

        classes.add(mianCla);

        for (Class<?> cla : classes) {
            Fill(cla);
        }
        for (Class<?> cla : classes) {
            autofill(cla);
        }
    }

    private static void autofill(Class<?> cla) {
        try {
            Map<String, Object> map = ObjMap.get(cla);
            if (map == null) return;
            Object obj = map.values().iterator().next();
            Field[] fields = cla.getDeclaredFields();
            Method[] methods = cla.getDeclaredMethods();
            for (Field field : fields) {
                fillField(cla, obj, field);
            }
            for (Method method : methods) {
                InitMethod(cla, obj, method);
            }
        } catch (Exception e) {
            Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);

        }
    }

    private static List<Method> startedMethods = new ArrayList<>();

    private static synchronized void InitMethod(Class<?> cla, Object obj, Method method) {
        try {
            if (method.isAnnotationPresent(Action.class)) {
                Action action = method.getAnnotation(Action.class);
                String[] sss = action.otherName();
                String acs = action.value();
                method.setAccessible(true);
                actions.put(acs, getEntry(obj, method));
                for (String str : sss) {
                    if (str.trim().isEmpty()) continue;
                    actions.put(str, getEntry(obj, method));
                }
            }
            if (method.isAnnotationPresent(Before.class)) {
                method.setAccessible(true);
                beforeS.put(cla, method);
            }
            if (method.isAnnotationPresent(After.class)) {
                method.setAccessible(true);
                afterS.put(cla, method);
            }
            if (method.isAnnotationPresent(Schedule.class)) {
                List<Map.Entry<String, Method>> list = timeMethods.get(cla);
                if (list == null) list = new ArrayList<>();
                Schedule sch = method.getAnnotation(Schedule.class);
                String[] ss = sch.value().split(",");
                for (String s : ss) {
                    list.add(getEntry(s, method));
                }
                timeMethods.put(cla, list);
            }
            if (method.isAnnotationPresent(TimeEve.class)) {
                long t = method.getAnnotation(TimeEve.class).value();
                if (t <= 0) return;
                if (!startedMethods.contains(method)) {
                    startedMethods.add(method);
                    final long ft = t;
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            try {
                                method.invoke(null);
                            } catch (Exception e) {
                                Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);
                            }
                        }
                    }, ft, ft);
                }
            }
        } catch (Exception e) {
            Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);
        }
    }

    private static final Timer timer = new Timer();

    private static final Map<Class<?>, List<Map.Entry<String, Method>>> timeMethods = new ConcurrentHashMap<>();

    public static final <T> T getContextValue(Class<?> claT, String id) {
        Map<String, Object> map = ObjMap.get(claT);
        if (map == null) return null;
        id = id == null ? map.keySet().iterator().next() : id;
        Object v = map.get(id);
        return (T) v;
    }

    private static void fillField(Class<?> cla, Object obj, Field field) {
        try {
            if (field.isAnnotationPresent(AutoStand.class)) {
                Map<String, Object> map = ObjMap.get(field.getType());
                if (map == null) return;
                String id = map.keySet().iterator().next();
                if (map.size() > 1) {
                    AutoStand auto = field.getAnnotation(AutoStand.class);
                    id = auto.id();
                }
                field.setAccessible(true);
                Object v = map.get(id);
                field.set(obj, v);
                Log("AutoStand Ok " + field.getType() + " On " + cla.getName(), 0);
            }
        } catch (Exception e) {
            Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);
        }
    }

    private static final void Fill(Class<?> cla) {
        try {
            if (cla.isAnnotationPresent(Controller.class)) {
                if (hasNoParameterConstructor(cla)) {
                    Object obj = newInstance(cla);
                    String id = cla.getAnnotation(Controller.class).value();
                    appendToObjMap(id, obj);
                } else {
                    Log(cla.getName() + "没有无参构造方法构建失败(don`t have NoParameters Constructor)", 2);
                }
            } else if (cla.isAnnotationPresent(Entity.class)) {
                if (hasNoParameterConstructor(cla)) {
                    Object obj = newInstance(cla);
                    String id = cla.getAnnotation(Entity.class).value();
                    appendToObjMap(id, obj);
                    startScanMainBean(cla);
                } else {
                    Log(cla.getName() + "没有无参构造方法构建失败(don`t have NoParameters Constructor)", 2);
                }
            }
        } catch (Exception e) {
            Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);
        }
    }

    private static final boolean hasNoParameterConstructor(Class<?> cla) {
        Constructor[] constructors = cla.getDeclaredConstructors();
        for (Constructor constructor : constructors) {
            if (constructor.getParameters().length == 0)
                return true;
        }
        return false;
    }

    private static final boolean maybe(final String res, final String par) {
        try {
            if (res.equals(par) || res.matches(par)) return true;
        } catch (Exception e) {
        }
        String par1 = par.substring(0, 1);
        if (par1.equals("\\"))
            par1 = par.substring(1, 2);
        if (res.startsWith(par1)) {
            int len = Math.min(res.length(), par.length());
            for (int i = 1; i < len; i++) {
                if (contians(par.charAt(i)))
                    return true;
                else {
                    if (res.charAt(i) != par.charAt(i))
                        return false;
                }
            }
        }
        return false;
    }

    private static final char[] chars = {'<', '.', '\\'};

    private static boolean contians(char c1) {
        for (char c : chars) {
            if (c == c1)
                return true;
        }
        return false;
    }

    private static final <K, V> Map.Entry<K, V> getEntry(K k, V v) {
        Map.Entry<K, V> entry = new Map.Entry<K, V>() {
            private final K _k = k;
            private V _v = v;

            @Override
            public K getKey() {
                return _k;
            }

            @Override
            public V getValue() {
                return _v;
            }

            @Override
            public V setValue(V v) {
                V v1 = _v;
                this._v = v;
                return v1;
            }
        };
        return entry;
    }

    private static Set<Class<?>> getClassName(String packageName, boolean isRecursion) {
        Set<String> classNames = null;
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace(".", "/");
        URL url = loader.getResource(packagePath);
        if (url != null) {
            String protocol = url.getProtocol().trim();
            if (protocol.equals("file")) {
                classNames = getClassNameFromDir(url.getPath(), packageName, isRecursion);
            } else if (protocol.equals("jar")) {
                JarFile jarFile = null;
                try {
                    jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                } catch (Exception e) {
                    Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);
                }
                if (jarFile != null) {
                    classNames = getClassNameFromJar(jarFile.entries(), packageName, isRecursion);
                }
            }
        } else {
            classNames = getClassNameFromJars(((URLClassLoader) loader).getURLs(), packageName, isRecursion);
        }
        System.out.println(classNames);
        Set<Class<?>> classes = new CopyOnWriteArraySet<>();
        for (String name : classNames) {
            try {
                Class<?> cla = loader.loadClass(name);
                classes.add(cla);
            } catch (ClassNotFoundException e) {
                Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);

            }
        }
        return classes;
    }

    private static Set<String> getClassNameFromDir(String filePath, String packageName, boolean isRecursion) {
        Set<String> className = new HashSet<>();
        File file = new File(filePath);
        File[] files = file.listFiles();
        for (File childFile : files) {

            if (childFile.isDirectory()) {
                if (isRecursion) {
                    className.addAll(getClassNameFromDir(childFile.getPath(), packageName + "." + childFile.getName(), isRecursion));
                }
            } else {
                String fileName = childFile.getName();
                if (fileName.endsWith(".class") && !fileName.contains("$")) {
                    className.add(packageName + "." + fileName.replace(".class", ""));
                }
            }
        }

        return className;
    }

    private static Set<String> getClassNameFromJar(Enumeration<JarEntry> jarEntries, String packageName, boolean isRecursion) {
        Set<String> classNames = new HashSet();
        while (jarEntries.hasMoreElements()) {
            JarEntry jarEntry = jarEntries.nextElement();
            if (!jarEntry.isDirectory()) {
                String entryName = jarEntry.getName().replaceAll("\\/", ".");
                if (entryName.endsWith(".class") && !entryName.contains("$") && entryName.startsWith(packageName)) {
                    entryName = entryName.replace(".class", "");
                    if (isRecursion) {
                        classNames.add(entryName);
                    } else if (!entryName.replace(packageName + ".", "").contains(".")) {
                        classNames.add(entryName);
                    }
                }
            }
        }

        return classNames;
    }

    private static Set<String> getClassNameFromJars(URL[] urls, String packageName, boolean isRecursion) {
        Set<String> classNames = new HashSet<>();

        for (int i = 0; i < urls.length; i++) {
            String classPath = urls[i].getPath();
            if (classPath.endsWith("classes/")) {
                continue;
            }
            JarFile jarFile = null;
            try {
                jarFile = new JarFile(classPath.substring(classPath.indexOf("/")));
            } catch (IOException e) {
                Log("存在一个异常(Has a Exception)=>" + e + " at " + getExceptionLine(e), -1);

            }

            if (jarFile != null) {
                classNames.addAll(getClassNameFromJar(jarFile.entries(), packageName, isRecursion));
            }
        }

        return classNames;
    }

    private static final class Result {
        private boolean isMatch = false;
        private static final Pattern pattern = Pattern.compile("<.*>");
        private Map<String, String> map = new HashMap<>();
        private boolean hasPar = false;
        private String K, V;
        private Object[] objs;
        private String res;
        private static final Map<String, Result> historyResult = new ConcurrentHashMap<>();
        private int state = -1;

        private synchronized static final Result create(final String res, final String par) {
            String end = String.format("%s|$|%s", res, par);
            if (historyResult.containsKey(end)) {
                return historyResult.get(end);
            }
            Result result = new Result();
            if (par.contains("<") && par.contains(">")) {
                Matcher matcher = pattern.matcher(par);
                if (matcher.find()) {
                    result.res = res;
                    String s1 = matcher.group();
                    String s2 = s1.substring(1, s1.length() - 1);
                    String[] ss = s2.split("=>");
                    if (ss.length == 1) {
                        String parN = par.replace("<" + ss[0] + ">", "") + s2;
                        if (res.matches(parN) || res.equals(parN)) {
                            result.isMatch = true;
                        } else {
                            result.isMatch = false;
                        }
                    } else {
                        String mat = ss[0];
                        int i = par.indexOf("<");
                        if (i >= res.length()) {
                            result.res = res;
                            if (res.matches(par) || res.equals(par)) {
                                result.isMatch = true;
                            } else {
                                result.isMatch = false;
                            }
                        } else {
                            String s3 = res.substring(i);
                            String s4 = par.substring(i);
                            String resB = res.substring(0, i);
                            String parB = par.substring(0, i);
                            Matcher m1 = Pattern.compile(mat).matcher(s3);
                            if (m1.matches() && resB.equals(parB)) {
                                result.hasPar = true;
                                result.K = ss[1];
                                result.V = m1.group();
                                result.isMatch = true;
                            } else result.isMatch = false;
                        }
                    }
                }
            } else {
                result.res = res;
                if (res.matches(par)) {
                    result.state = 1;
                    result.isMatch = true;
                } else if (res.equals(par)) {
                    result.state = 0;
                    result.isMatch = true;
                } else {
                    result.isMatch = false;
                }
            }
            if (result.isMatch) {
                historyResult.put(String.format("%s|$|%s", res, par), result);
            }
            return result;
        }

        public String getRes() {
            return res;
        }


        public boolean isMatch() {
            return isMatch;
        }

        public Object[] getObjs() {
            return objs;
        }

        public void setObjs(Object... objs) {
            this.objs = objs;
        }

        public static Pattern getPattern() {
            return pattern;
        }

        public Map<String, String> getMap() {
            return map;
        }

        public boolean isHasPar() {
            return hasPar;
        }

        public String getK() {
            return K;
        }

        public String getV() {
            return V;
        }
    }

    private static final class Utils {
        private static final SimpleDateFormat myFmt = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");

        public static long getTimeFromNowTo(int hour, int mini, int mil) {
            Date date = null;
            try {
                String p1 = String.format("%s-%s-%s-%s-%s-%s", getYear(), getMon(), getDay(), hour, mini, mil);
                date = myFmt.parse(p1);

            } catch (Exception e) {
            }
            long millis = date.getTime();
            long now = System.currentTimeMillis();
            return millis - now;
        }

        private static int getYear() {
            String s = myFmt.format(new Date());
            return Integer.parseInt(s.substring(0, 4));
        }

        private static int getMon() {
            String s = myFmt.format(new Date());
            return Integer.parseInt(s.substring(5, 7));
        }

        private static int getDay() {
            String s = myFmt.format(new Date());
            return Integer.parseInt(s.substring(8, 10));
        }
    }
}