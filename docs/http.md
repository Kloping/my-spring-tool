## http 网络 请求 注解化

这就类似于 feign 或者说 根据其做出来的 但是 相对于 feign [HttpClientStarter]() 更轻便

- ## 如何使用

在 能被扫描的范围内 建一个 interface 并用 @HttpClient(主机地址)
例如:

```java
import io.github.kloping.MySpringTool.annotations.http.HttpClient;
import io.github.kloping.MySpringTool.annotations.http.PostPath;
import io.github.kloping.MySpringTool.annotations.http.RequestBody;

@HttpClient("https://glot.io/run/")
public interface H2 {

    /**
     * //@RequestBody(type = "json") 
     * 意为: 将其对象 以 JSON 格式 作为 post 请求体
     * //@RequestBody()
     * 意为: 将调用对象 toString 作为 post 请求体
     * 其中 Path 暂时不支持变更
     * @param entity
     * @return
     */
    @PostPath("java?version=latest")
    CodeResponse m1(@RequestBody(type = "json") CodeEntity entity);
}


```

```java
import io.github.kloping.MySpringTool.annotations.http.GetPath;
import io.github.kloping.MySpringTool.annotations.http.HttpClient;
import io.github.kloping.MySpringTool.annotations.http.ParamBody;
import io.github.kloping.MySpringTool.annotations.http.ParamName;

@HttpClient("http://49.232.209.180:20041/")
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
import io.github.kloping.MySpringTool.annotations.Controller;

@Controller
public class Main {
    //自动实现并填充
    @AutoStand
    H1 h1;
    
    @AutoStand
    H2 h2;
}
```