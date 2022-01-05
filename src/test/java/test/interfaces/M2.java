package test.interfaces;

import io.github.kloping.MySpringTool.annotations.http.*;

import java.util.Map;

@HttpClient("")
public interface M2 {
    @GetPath("https://v.quelingfei.com:4438//sssv.php?")
    String doc(
            @ParamName("top") @DefaultValue("10") Integer top
            , @ParamName("q") String keyword
            , @Headers Map<String, String> map);
}
