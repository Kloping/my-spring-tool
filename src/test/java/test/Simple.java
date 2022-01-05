package test;

import io.github.kloping.MySpringTool.StarterApplication;
import io.github.kloping.MySpringTool.annotations.AutoStand;
import io.github.kloping.MySpringTool.annotations.CommentScan;
import test.entitys.pvpQQH0.PvpQQH0;
import test.interfaces.M2;
import test.interfaces.PvpQq;
import test.interfaces.SearchPics;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@CommentScan(path = "test")
public class Simple {
    public static void main(String[] args) {
        StarterApplication.addConfFile("./src/test/java/conf.txt");
        StarterApplication.run(Simple.class);

//        PvpQQH0 v2 = pvpQq.get1(null);
        Map<String, String> maps = new HashMap<>();
        maps.put("referer", "https://www.feijisu09.com/");
        maps.put("sec-fetch-site", "cross-site");
        maps.put("accept-language", "zh-CN,zh;q=0.9,en;q=0.8,en-GB;q=0.7,en-US;q=0.6");
        maps.put("origin", "https://www.feijisu09.com");
        maps.put("pragma", "no-cache");
        maps.put("accept", "*/*");
        maps.put("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"96\", \"Microsoft Edge\";v=\"96\"");
        maps.put("sec-ch-ua-mobile", "?0");
        maps.put("sec-ch-ua-platform", "\"Windows\"");
        maps.put("cache-control", "no-cache");
        maps.put("accept-encoding", "gzip, deflate, br");
        maps.put("sec-fetch-dest", "empty");

        System.out.println(m2.doc(10, "斗罗大陆", maps));

        System.out.println("end");

    }

    public static String c1(String arg) {
        int i1 = arg.indexOf("(");
        int i2 = arg.lastIndexOf(")");
        return arg.substring(i1 + 1, i2);
    }

    @AutoStand
    static SearchPics searchPics;

    @AutoStand
    static M2 m2;

    @AutoStand
    static PvpQq pvpQq;

}
