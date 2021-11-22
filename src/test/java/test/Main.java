package test;

import io.github.kloping.MySpringTool.StarterApplication;
import io.github.kloping.MySpringTool.annotations.*;
import io.github.kloping.MySpringTool.entity.Runner;
import io.github.kloping.MySpringTool.exceptions.NoRunException;
import test.interfaces.M2;

import java.lang.reflect.InvocationTargetException;

@CommentScan(path = "test")
@Controller
public class Main {
    @Before
    public void before(String arg) {
        System.out.println("before => " + arg);
    }

    @Action("a")
    public void m1() {
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

    @Action("a<.*?=>s>c")
    public void m4(@Param("s") String s) {
        System.out.println(s);
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

    public static void main(String[] args) throws IllegalAccessException, InvocationTargetException {
        StarterApplication.addConfFile("./src/test/java/conf.txt");
        StarterApplication.setMainKey(Long.class);
        StarterApplication.setAccessTypes(String.class, Integer.class);
        StarterApplication.setAllBefore(new Runner(Runner.state.BEFORE) {
            @Override
            public void run(Object t, Object[] objects) throws NoRunException {
                System.out.println("all after");
            }
        });
        StarterApplication.run(Main.class);
        StarterApplication.ExecuteMethod(1000L, "a", "我是参数", 111111);
        StarterApplication.ExecuteMethod(1001L, "ab", "我是参数", 111111);
        StarterApplication.ExecuteMethod(1002L, "abc", "我是参数", 111111);
        StarterApplication.ExecuteMethod(1002L, "adsadbc", "我是参数", 111111);
        StarterApplication.ExecuteMethod(1002L, "abffdsfc", "我是参数", 111111);
        StarterApplication.ExecuteMethod(1002L, "abeeeeec", "我是参数", 111111);
        System.out.println("===========");
        System.out.println(m2.doc());
    }

    @AutoStand
    static M2 m2;

    @AutoStand(id = "k1")
    static Boolean k1;
}
