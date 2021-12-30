package test.interfaces;

import io.github.kloping.MySpringTool.annotations.http.Callback;
import io.github.kloping.MySpringTool.annotations.http.GetPath;
import io.github.kloping.MySpringTool.annotations.http.HttpClient;
import io.github.kloping.MySpringTool.annotations.http.ParamName;
import test.entitys.pvpQQH0.PvpQQH0;
import test.entitys.pvpQQVoice.PvpQQVoice;

/**
 * @author github kloping
 * @version 1.0
 * @date 2021/12/30-9:54
 */
@HttpClient("https://pvp.qq.com/")
public interface PvpQq {
    /**
     * get data voice
     *
     * @param createList
     * @return
     */
    @GetPath("zlkdatasys/data_zlk_lb.json")
    @Callback("test.Simple.c1")
    PvpQQVoice get0(@ParamName("callback") String createList);

    @GetPath("webplat/info/news_version3/15592/18024/23901/24397/24398/m22352/index.shtml?callback=createHeroList")
    @Callback("test.Simple.c1")
    PvpQQH0 get1(@ParamName("callback") String createHeroList);
}
