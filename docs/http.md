## http 网络 请求 注解化

这就类似于 feign 或者说 根据其做出来的 但是 相对于 feign [HttpClientStarter]() 更轻便

- ## 如何使用

在 能被扫描的范围内 建一个 interface 并用 @HttpClient(主机地址)
例如:

```java
import io.github.kloping.spt.annotations.http.HttpClient;
import io.github.kloping.spt.annotations.http.ParamName;
import io.github.kloping.spt.annotations.http.PostPath;
import io.github.kloping.spt.annotations.http.RequestBody;

@HttpClient("https://glot.io/run/")
public interface H2 {

    /**
     * //@RequestBody(type = "json") 
     * 意为: 将其对象 以 JSON 格式 作为 post 请求体
     * //@RequestBody()
     * 意为: 将调用对象 toString 作为 post 请求体
     * @param entity
     * @return
     */
    @PostPath("java")
    CodeResponse m1(
            @RequestBody(type = "json") CodeEntity entity
            , @ParamName("version") String version
    );
}


```

```java
import io.github.kloping.spt.annotations.http.GetPath;
import io.github.kloping.spt.annotations.http.HttpClient;
import io.github.kloping.spt.annotations.http.ParamBody;
import io.github.kloping.spt.annotations.http.ParamName;

@HttpClient("http://kloping.top/")
public interface H1 {

    /**
     * 将 request 字段 转为 &k=v&...形式访问
     * @param request
     * @return
     */
    @GetPath("/api/search/pic")
    JSONObject m1(@ParamBody Request1 request);

    /**
     * 可变长 参数
     *  new ParamsBuilder().append(k,v).build()
     * @param params
     * @return
     */
    @GetPath("/api/search/pic")
    JSONObject m1(Params params);


    /**
     * 定长get 路径 参数
     * @param keyword
     * @param num
     * @param type
     * @return
     */
    @GetPath("/api/search/pic")
    JSONObject m1(@ParamName("keyword") String keyword
            , @ParamName("num") Integer num
            , @ParamName("type") String type
    );

}
```

```java
import io.github.kloping.spt.annotations.Controller;

@Controller
public class Main {
    //自动实现并填充
    @AutoStand
    H1 h1;

    @AutoStand
    H2 h2;
}
```

```java

@HttpClient("https://image.baidu.com/")
public interface MTest {

    @GetPath("search/acjson")
    @CookieFrom(value = "this", method = "GET")
    Response0 get(@ParamBody RequestEntity entity);
}

```

* CookieFrom 为了 解决 反爬虫而设计
* @CookieFrom(values,method)
    * values: 从哪(些)网址上获取 Cookie
        * 其中 this 代表 当前的地址 即 即将访问的网址 将被
        * 访问两次 第一次获取 Cookie 第二次返回真实数据
    * method: 获取方法 默认 GET 且 仅可为 GET POST

> 2024.6.27 更新

```java
@HttpClient("http://{sptest.Main.net}")
public interface Baidu {
    @GetPath("test")
    String test();
}
```

```java
package sptest;

public class Main {
    public static String net = "kloping.top/";
}
```

> >resp status code 200 from the [http://kloping.top/test]

