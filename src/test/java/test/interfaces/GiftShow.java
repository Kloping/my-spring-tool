package test.interfaces;

import io.github.kloping.MySpringTool.annotations.http.*;

@HttpClient("https://www.kuaishou.com/")
public interface GiftShow {
    @GetPath("search/video")
    String doc(@ParamName("searchKey") String key);

    @PostPath("graphql")
    @CookieFrom(value = {"https://www.kuaishou.com/"}, method = "GET")
    byte[] doc2(@RequestBody() String data);
}
