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
