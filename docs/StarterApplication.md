## [StarterApplication](https://github.com/Kloping/my-spring-tool/blob/master/src/main/java/io/github/kloping/MySpringTool/StarterApplication.java)

相对于旧版 此 实现更加分理化 使用了 基础的设计模式编写<br>

更加的便于 修改其功能 与 用户的自定义功能

更便于日后维护

- [interfaces](https://github.com/Kloping/my-spring-tool/tree/master/src/main/java/io/github/kloping/MySpringTool/interfaces)
    - 定义了基础的功能 但没有具体的实现
- [h1](https://github.com/Kloping/my-spring-tool/tree/master/src/main/java/io/github/kloping/MySpringTool/h1)
    - 完成了基础的功能实现
- [StarterApplication.Setting](https://github.com/Kloping/my-spring-tool/blob/master/src/main/java/io/github/kloping/MySpringTool/StarterApplication.java)
    - 系统 默认配置 与 功能的协调启动
    - 可根据具体需要更改配置 实现自己需要的功能

基础案例1:[Simple.java]()

```java

package test;

import io.github.kloping.MySpringTool.StarterApplication;
import io.github.kloping.MySpringTool.annotations.AutoStand;
import io.github.kloping.MySpringTool.annotations.CommentScan;
import test.interfaces.M2;

@CommentScan(path = "test")
public class Simple {
    public static void main(String[] args) {
        StarterApplication.addConfFile("./src/test/java/conf.txt");
        StarterApplication.run(Simple.class);
        System.out.println(m2.doc());
    }

    @AutoStand
    static M2 m2;
}

```

基础案例2:
<details> 
<summary><a href="#">Main.java</a></summary> 

```java
package old;

import io.github.kloping.MySpringTool.StarterApplication;
import io.github.kloping.MySpringTool.annotations.*;
import io.github.kloping.MySpringTool.entity.interfaces.Runner;
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
        StarterApplication.setAccessTypes(String.class, Number.class);
        StarterApplication.setAllBefore(new Runner(Runner.state.BEFORE) {
            @Override
            public void run(Object t, Object[] objects) throws NoRunException {
                System.out.println("all after");
            }
        });
        StarterApplication.run(Main.class);
        StarterApplication.executeMethod(1000L, "a", "我是参数", 111111);
        StarterApplication.executeMethod(1001L, "ab", "我是参数", 111111);
        StarterApplication.executeMethod(1002L, "abc", "我是参数", 111111);
        StarterApplication.executeMethod(1002L, "adsadbc", "我是参数", 111111);
        StarterApplication.executeMethod(1002L, "abffdsfc", "我是参数", 111111);
        StarterApplication.executeMethod(1002L, "abeeeeec", "我是参数", 111111);
        System.out.println("===========");
        System.out.println(m2.doc());
    }

    @AutoStand
    static M2 m2;

    @AutoStand(id = "k1")
    static Boolean k1;
}

```

</details>



<hr>

update time on 21/11/22:11

* 允许用户自定义 Setting
* version for 0.2.6-M1

<hr>
update time on 21/11/22:16

* 修复 bug 
* version for 0.2.6-M2
