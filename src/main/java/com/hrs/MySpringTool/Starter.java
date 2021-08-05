package com.hrs.MySpringTool;


import com.hrs.MySpringTool.annotations.*;
import com.hrs.MySpringTool.exceptions.NoRunException;

import java.io.File;
import java.io.IOException;
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

/**
 * 使用 Starter.run(Main.class)来启动程序,前提启动类上必须存在CommentScan注解,已说明要扫描的包
 * 设置 主键 Key 类型
 * 设置 接受的 数据组 类型
 * 通过ExecuteMethod()来调用 注意 参数
 * new Object[]{
 * 主键Key,//_key类型
 * 要匹配的action,//String 对应 @Action(的内容)
 * ... //接受的参数类型组
 * };
 *
 * @Action 用法
 * ("abcd")
 * 匹配 ("abcd") //必须一样
 * ("abc.")   //若存在正则表达
 * 匹配 ("abc.")("abcd")("abc1")("abca")
 * ("abc<.+=>name>")   //若存在正则表达 在接下来的方法内的参数 @Param("name") String name 自动赋值 为 .+ 匹配的值
 * 匹配 ("abc.")("abcd")("abc1")("abca")
 */
public final class Starter {
    private static String scanPath = "";
    private static Object main;
    private static Class<?>[] accPars = null;
    private static Class<?> _key = null;
    private static Integer Log_Level = 0;

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
            scanPath = scan.path();
            Log("开始扫描主类 Bean(Start Scan Main Class Bean)", 1);
            startScanMainBean(cla);
            Log("扫描主类 Bean 完成(Scan Main Class Bean Complete)", 1);
            Log("开始 扫描 所有 包(Start Scan All Class On Package)", 1);
            startScan();
            Log("准备完成(All is Paper)", 1);
        } else {
            throw new NoRunException("此类上必须存在 CommentScan 注解 (class must has @interface CommentScan )");
        }
    }

    public static void setAllAfter(AllAfterOrBefore allAfter) {
        Starter.allAfter = allAfter;
    }

    public static void setAllBefore(AllAfterOrBefore allBefore) {
        Starter.allBefore = allBefore;
    }

    public static void setLog_Level(Integer log_Level) {
        Log_Level = log_Level;
    }

    public static final int ExecuteMethod(Object... objects) {
        final Object[] objs = BaseToPack(objects);
        if (accept(objs)) {
            Log("开始匹配=>" + Arrays.toString(objects), 0);
            if (!runList.add(objs[0])) {
                runQueue.offer(objs);
                Log("稍后执行-->" + Arrays.toString(objs), 2);
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
                                Run(objs);
                            }
                        });
                        try {
                            future.get(15, TimeUnit.SECONDS);
                        } catch (InterruptedException e) {
                            Log("运行时错误(Running Has Error)=>" + e, -1);
                            future.cancel(true);
                        } catch (TimeoutException e) {
                            Log("运行超时(Run Time Out)=>" + e, -1);
                            future.cancel(true);
                        } catch (Exception e) {
                            Log("其他错误(Other Error)=>" + e, -1);
                            future.cancel(true);
                        }
                        Log("活动处理结束(Run Complete)=>" + Arrays.toString(objs), 0);
                        runList.remove(objs[0]);
                        Log("耗时(Use Timed)=>" + (System.currentTimeMillis() - l1) + "ms", 1);
                        Log("============================================", 1);
                        if (!runQueue.isEmpty()) {
                            Object[] objects = runQueue.poll();
                            Log("开始回调 =>" + Arrays.toString(objects), 1);
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
     * 如果 不需要扫描 或者 不支持扫描 则设置为 false 并把类元素加入 classes 集合
     */
    public static final boolean NeedScan = true;

    /**
     * 阅读 NessScan 注解
     */
    public static final Set<Class<?>> AllClass = new CopyOnWriteArraySet<>();
    private static abstract class A implements Runnable{
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
        info = "[com.hsr.Spring]" + info;
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

    /**
     * 所有基本类型转化为 包装类型
     *
     * @param objects
     * @return
     */
    private static Object[] BaseToPack(Object[] objects) {
        Object[] objects1 = new Object[objects.length];
        for (int i = 0; i < objects.length; i++) {
            if (objects[i].getClass() == byte.class) {
                objects1[i] = Byte.valueOf(String.valueOf(objects[i]));
            } else if (objects[i].getClass() == short.class) {
                objects1[i] = Short.valueOf(String.valueOf(objects[i]));
            } else if (objects[i].getClass() == int.class) {
                objects1[i] = Integer.valueOf(String.valueOf(objects[i]));
            } else if (objects[i].getClass() == long.class) {
                objects1[i] = Long.valueOf(String.valueOf(objects[i]));
            } else if (objects[i].getClass() == boolean.class) {
                objects1[i] = Boolean.valueOf(String.valueOf(objects[i]));
            } else if (objects[i].getClass() == float.class) {
                objects1[i] = Float.valueOf(String.valueOf(objects[i]));
            } else if (objects[i].getClass() == double.class) {
                objects1[i] = Double.valueOf(String.valueOf(objects[i]));
            } else objects1[i] = objects[i];
        }
        return objects1;
    }

    private static Class<?> BaseToPack(Class<?> cla) {
        if (cla == byte.class) return Byte.class;
        if (cla == short.class) return Short.class;
        if (cla == int.class) return Integer.class;
        if (cla == long.class) return Long.class;
        if (cla == boolean.class) return Boolean.class;
        if (cla == char.class) return Character.class;
        if (cla == float.class) return Float.class;
        if (cla == double.class) return Double.class;
        return cla;
    }

    /**
     * 运行
     *
     * @param objs
     */
    private static void Run(Object... objs) {
        for (String v : actions.keySet()) {
            String res = objs[1].toString();
            if (!mabey(res, v)) continue;
            Result result = null;
            if ((result = new Result(res, v)).isMatch()) {
                Log("匹配并运行(mather and run)=>" + Arrays.toString(objs), 1);
                result.setObjs(objs);
                RunMethod(actions.get(v), result);
                return;
            }
        }
        Log("无匹配 (no mather)=>" + Arrays.toString(objs), 2);
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
                        if (!RunBeforeOrAfter(afterS.get(cla), objectMethodEntry.getKey(), new Object[]{ret}, result))
                            return;

                        allAfter.run(ret, objs);
                    } else {
                        allAfter.run(ret, objs);
                        if (!RunBeforeOrAfter(afterS.get(cla), objectMethodEntry.getKey(), new Object[]{ret}, result))
                            return;
                    }
                } else {
                    if (!RunBeforeOrAfter(afterS.get(cla), objectMethodEntry.getKey(), new Object[]{ret}, result))
                        return;
                }
            } else if (allAfter != null) {
                allAfter.run(ret, objs);
            }
        } catch (Exception e) {
            if (e instanceof InvocationTargetException) {
                InvocationTargetException ite = (InvocationTargetException) e;
                if (ite.getTargetException().getClass() == NoRunException.class) {
                    NoRunException exception = (NoRunException) ite.getTargetException();
                    Log("抛出 不运行异常(throw NuRunException): " + exception.getMessage(), 2);
                } else {
                    Log("存在映射一个异常(Has a Invoke Exception)=>" + ite.getTargetException(), -1);
                }
                return;
            }
            Log("存在一个异常(Has a Exception)=>" + e, -1);
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
                InvocationTargetException ite = (InvocationTargetException) e;
                if (ite.getTargetException().getClass() == NoRunException.class) {
                    NoRunException exception = (NoRunException) ite.getTargetException();
                    Log("抛出 不运行异常(throw NuRunException): " + exception.getMessage(), 2);
                } else {
                    Log("存在映射一个异常(Has a Invoke Exception)=>" + ite.getTargetException(), -1);
                }
                return false;
            }
            Log("存在一个异常(Has a Exception)=>" + e, -1);
            return false;
        }
    }

    private static Object[] AutoObjFromPar(Parameter[] parameters, Object[] objects) {
        Object[] objects1 = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            if (hasAnnotation(parameters[i])) continue;
            Class<?> type = BaseToPack(parameters[i].getType());
            int n = find(objects, type);
            if (n >= 0) objects1[i] = objects[n];
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
                        objects[i] = result.getV();
                    }
            } else if (parameters[i].isAnnotationPresent(AllMess.class)) {
                AllMess param = parameters[i].getAnnotation(AllMess.class);
                objects[i] = result.getRes();
            }
            continue;
        }
        return objects;
    }

    private static int find(Object[] obj, Class<?> cla) {
        obj = BaseToPack(obj);
        int n = -1;
        for (int i = 2; i < obj.length; i++) {
            if (superOrImpl(cla, obj[i].getClass()))
                n = i;
        }
        return n;
    }

    private static boolean accept(Object... objs) {
        if (objs[0].getClass() == _key) {
            if (objs[1].getClass() == String.class) {
                for (int i = 2; i < accPars.length + 2; i++) {
                    Class<?> cla1 = objs[i].getClass();
                    Class<?> cla2 = accPars[i - 2];
                    boolean k = superOrImpl(cla2, cla1);
                    if (k)
                        continue;
                    else return false;
                }
                return true;
            }
        }
        return false;
    }

    private static boolean superOrImpl(final Class<?> father, final Class<?> son) {
        if (father == son) return true;
        Class<?> son1 = son;
        Class<?> father1 = father;
        while (hasSuper(son1)) {
            father1 = son1.getSuperclass();
            if (father1 == son) return true;
            else son1 = father1;
        }
        Class<?>[] classes1 = son.getInterfaces();
        if (isInterfaces(classes1, father))
            return true;
        return false;
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

    private static void startScanMainBean(Class<?> cla) {
        try {
            main = cla.newInstance();
            Method[] methods = cla.getDeclaredMethods();
            for (Method method : methods) {
                if (method.isAnnotationPresent(Bean.class)) {
                    try {
                        Bean bean = method.getAnnotation(Bean.class);
                        String id = bean.value();
                        Class rec = method.getReturnType();
                        Object obj = method.invoke(main);
                        appendToObjMap(id, obj, rec);
                    } catch (Exception e) {
                        Log("存在一个异常(Has a Exception)=>" + e, -1);

                    }
                }
            }
        } catch (Exception e) {
            Log("存在一个异常(Has a Exception)=>" + e, -1);

        }
    }

    private static void startScan() {
        if (NeedScan) {
            Set<Class<?>> sets = getClassName(scanPath, true);
            classes.addAll(sets);
        } else
            classes.addAll(AllClass);
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
            Log("存在一个异常(Has a Exception)=>" + e, -1);

        }
    }

    private static void InitMethod(Class<?> cla, Object obj, Method method) {
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
        } catch (Exception e) {
            Log("存在一个异常(Has a Exception)=>" + e, -1);

        }
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
            Log("存在一个异常(Has a Exception)=>" + e, -1);
        }
    }

    private static final void Fill(Class<?> cla) {
        try {
            if (cla.isAnnotationPresent(Controller.class)) {
                if (hasNoParameterConstructor(cla)) {
                    Object obj = cla.newInstance();
                    String id = cla.getAnnotation(Controller.class).value();
                    appendToObjMap(id, obj);
                }
            } else if (cla.isAnnotationPresent(Entity.class)) {
                if (hasNoParameterConstructor(cla)) {
                    Object obj = cla.newInstance();
                    String id = cla.getAnnotation(Entity.class).value();
                    appendToObjMap(id, obj);
                } else {
                    Log(cla.getName() + "没有无参构造方法构建失败(don`t have NoParameters Constructor)", 2);
                }
            }
        } catch (Exception e) {
            Log("存在一个异常(Has a Exception)=>" + e, -1);
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

    private static final boolean mabey(final String res, final String par) {
        if (res.equals(par)) return true;
        String par1 = par.substring(0, 1);
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
            String protocol = url.getProtocol();
            if (protocol.equals("file")) {
                classNames = getClassNameFromDir(url.getPath(), packageName, isRecursion);
            } else if (protocol.equals("jar")) {
                JarFile jarFile = null;
                try {
                    jarFile = ((JarURLConnection) url.openConnection()).getJarFile();
                } catch (Exception e) {
                    Log("存在一个异常(Has a Exception)=>" + e, -1);
                }
                if (jarFile != null) {
                    getClassNameFromJar(jarFile.entries(), packageName, isRecursion);
                }
            }
        } else {
            classNames = getClassNameFromJars(((URLClassLoader) loader).getURLs(), packageName, isRecursion);
        }
        Set<Class<?>> classes = new CopyOnWriteArraySet<>();
        for (String name : classNames) {
            try {
                Class<?> cla = loader.loadClass(name);
                classes.add(cla);
            } catch (ClassNotFoundException e) {
                Log("存在一个异常(Has a Exception)=>" + e, -1);

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
                String entryName = jarEntry.getName().replace("/", ".");
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
                Log("存在一个异常(Has a Exception)=>" + e, -1);

            }

            if (jarFile != null) {
                classNames.addAll(getClassNameFromJar(jarFile.entries(), packageName, isRecursion));
            }
        }

        return classNames;
    }

    private static final class Result {
        private boolean isMatch = false;
        private static final Pattern pattern = Pattern.compile("<.{0,}>");
        private Map<String, String> map = new HashMap<>();
        private boolean hasPar = false;
        private String K, V;
        private Object[] objs;
        private String res;

        public Result(final String res, final String par) {
            if (par.contains("<") && par.contains(">")) {
                Matcher matcher = pattern.matcher(par);
                if (matcher.find()) {
                    this.res = res;
                    String s1 = matcher.group();
                    String s2 = s1.substring(1, s1.length() - 1);
                    String[] ss = s2.split("=>");
                    String mat = ss[0];
                    int i = par.indexOf("<");
                    String s3 = res.substring(i);
                    Matcher m1 = Pattern.compile(mat).matcher(s3);
                    if (m1.matches()) {
                        hasPar = true;
                        K = ss[1];
                        V = m1.group();
                        isMatch = true;
                    } else isMatch = false;
                }
            } else {
                this.res = res;
                if (res.matches(par) || res.equals(par)) {
                    this.isMatch = true;
                } else {
                    isMatch = false;
                }
            }
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
}