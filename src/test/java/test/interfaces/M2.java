package test.interfaces;

import io.github.kloping.MySpringTool.annotations.http.GetPath;
import io.github.kloping.MySpringTool.annotations.http.HttpClient;

@HttpClient("http://www.baidu.com")
public interface M2 {
    @GetPath("/")
    String doc();
}
