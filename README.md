# Spring式 浓缩式 分支处理 框架

## 声明:

### 一切开发旨在学习，请勿用于非法用途

  <h4>
  <li>此框架由GitHhub: <a href="https://github.com/kloping"> Kloping </a> 开发</li>
  <li>此框架 是完全免费且开放源代码</li>
  <li>且 仅用于 学习和娱乐<u><b><i>禁止用于非法用途</i></b></u>
</h4>

使用Maven pom.xml

```xml

<dependencies>
    <dependency>
        <groupId>io.github.Kloping</groupId>
        <artifactId>SpringTool</artifactId>
        <version>0.6.3-R1</version>
    </dependency>
</dependencies>

```

### 功能列表

- action 字符匹配执行 类似springboot@controller执行方式 不过不需要web 路径改为匹配字符串模式执行
- 自动生成网络访问实现类 类似feign [文档](docs/http.md)
- 自动实例化 实体类 类似springboot@bean
- 自动填充类内字段 类springboot@Autowired
- 可扩展框架

> 应用场景一

QQ官方机器人 Java/JVM/kotlin [SDK](https://github.com/Kloping/qqpd-bot-java)

> 应用场景二

不知名game[a game for Cultivation and Turn](https://github.com/Kloping/mihdp)



```java

package sptest;

import io.github.kloping.arr.Class2OMap;
import io.github.kloping.spt.StarterObjectApplication;
import io.github.kloping.spt.annotations.*;
import io.github.kloping.spt.interfaces.entitys.MatherResult;

/**
 * @author github-kloping
 * @version 1.0
 */
@ComponentScan("sptest")
@Controller
public class Main {
    public static void main(String[] args) {
        StarterObjectApplication application = new StarterObjectApplication(Main.class.getClassLoader());
        application.run0(Main.class);
        application.executeMethod(1L, "你好", "data");
    }

    @Action("你.*?")
    public Integer s0(@AllMess String p, String arg) {
        System.out.println("触发action " + p + " for " + arg);
        return 999;
    }

    @DefAction
    public void def(@AllMess String s, MatherResult result, Class2OMap class2OMap) {
        System.out.println("非action");
    }

    @Before
    public Object before(MatherResult result, Class2OMap class2OMap) {
        System.out.println("触发之前 before");
        return null;
    }

    @After
    public void after(Object d) {
        System.out.println("触发之后 after " + d);
    }
}

```
输出为: ↓↓
```text
[github.kloping.ST][Info]  [06/26-17:28:38:405]=>version 0.6.3-R1 sptool start success
[github.kloping.ST][Normal][06/26-17:28:38:406]=>计时任务结束...
触发之前 before
触发action 你好 for data
触发之后 after [Ljava.lang.Object;@523c947c
[github.kloping.ST][Info]  [06/26-17:28:38:423]=>lost time 6 Millisecond
```