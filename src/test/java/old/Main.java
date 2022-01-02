package old;

import io.github.kloping.MySpringTool.StarterApplication;
import io.github.kloping.MySpringTool.annotations.*;
import io.github.kloping.MySpringTool.entity.impls.RunnerEve;
import io.github.kloping.MySpringTool.exceptions.NoRunException;
import io.github.kloping.MySpringTool.h1.impl.component.ExecutorNowImpl;
import io.github.kloping.MySpringTool.h1.impls.baseup.QueueExecutorWithReturnsAndInterceptorImpl;
import io.github.kloping.MySpringTool.interfaces.component.Callback;
import io.github.kloping.MySpringTool.interfaces.component.Filter;
import io.github.kloping.arr.Class2OMap;
import test.entitys.Group;
import test.entitys.User;
import test.interfaces.M2;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

@CommentScan(path = "test")
@Controller
public class Main {
    @Before
    public void before(String arg) {
        System.out.println("before => " + arg);
    }

    @Action("a")
    public void m1(String m1, Number m2, Group group, User user, Class2OMap class2OMap) {
        System.out.println("m1");
    }

    @Action("a.+")
    public void m2() {
        System.out.println("m2");
    }

    @Action("a<b=>s>")
    public void m3() {
        System.out.println("m3");
    }

    @Action("abcdef<.{0,}>")
    public void m4(@Param("name") String s) {
        System.out.println("==============================>>>>>>>");
        System.out.println("m4");
//        try {
//            Thread.sleep(1500);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
    }

    @After
    public void after() {
        System.out.println("after");
    }

    @TimeEve(1000)
    public void s1() {
        System.out.println("1000");
    }

    @Schedule("10:05:00")
    public void s2() {
        System.out.println("333333333333333333333333333");
    }

    public static void main(String[] args) throws IllegalAccessException, InvocationTargetException, InterruptedException {
        new StarterApplication.Setting() {
            @Override
            protected void defaultInit() {
                executor = new ExecutorNowImpl();
                queueExecutor = QueueExecutorWithReturnsAndInterceptorImpl
                        .create(Long.class, 25, 10 * 1000, executor);
                super.defaultInit();
            }
        }.defaultInit();
        StarterApplication.addConfFile("./src/test/java/conf.txt");
        StarterApplication.setMainKey(Long.class);
        StarterApplication.setAccessTypes(String.class, Number.class);
        StarterApplication.setAllBefore(new RunnerEve() {
            @Override
            public void methodRuined(Object ret, Method method, Object t, Object... objects) {
                System.out.println("any method before or after");
            }

            /**
             * this method not run if executor instanceof QueueExecutorWithReturnsImpl
             *
             * @param t
             * @param objects
             * @throws NoRunException
             */
            @Override
            public void run(Object t, Object[] objects) throws NoRunException {
            }
        });
        StarterApplication.run(Main.class);
        Group group = new Group(10L, "nick name");
        try {
            QueueExecutorWithReturnsAndInterceptorImpl queueExecutorWithReturnsAndInterceptor =
                    StarterApplication.Setting.INSTANCE.getContextManager()
                            .getContextEntity(QueueExecutorWithReturnsAndInterceptorImpl.class);
            queueExecutorWithReturnsAndInterceptor.addIntercept(1, new Callback() {
                @Override
                public void call(Object... objects) {
                    System.out.println("intercepted==========");
                }
            }, new Filter() {
                @Override
                public boolean filter(Object... o) {
                    if (Long.parseLong(o[0].toString()) == 1002L) return true;
                    return false;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
//
//
        User user = new User(1L, 10L, "nickname", "name");
        StarterApplication.ExecuteMethod(1001L, "abcdef bbhh", "我是", 111111);
        StarterApplication.ExecuteMethod(1001L, "aaaaf bbhh", "我是", 111111);
        Thread.sleep(1000);
        Thread.sleep(1000);
        StarterApplication.Setting.INSTANCE.getActionManager().replaceAction("abcdef<.{0,}>","aaaa.+");
        StarterApplication.ExecuteMethod(1001L, "abcdef bbhh", "我是", 111111);
        StarterApplication.ExecuteMethod(1001L, "aaaaf bbhh", "我是", 111111);

        System.out.println("===========");
//        System.out.println(m2.doc());
    }

    @AutoStand
    static M2 m2;

    @AutoStand(id = "k1")
    static Boolean k1;
}
